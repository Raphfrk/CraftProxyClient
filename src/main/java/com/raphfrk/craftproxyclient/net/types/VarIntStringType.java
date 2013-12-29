/*
 * This file is part of CraftProxyClient.
 *
 * Copyright (c) 2013-2014, Raphfrk <http://raphfrk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.raphfrk.craftproxyclient.net.types;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class VarIntStringType extends Type<String> {

	public boolean writeRaw(String data, ByteBuffer buf) {
		byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
		if (buf.remaining() >= stringToLength(bytes)) {
			int stringLength = bytes.length;
			VarIntType.writeRaw(stringLength, buf);
			buf.put(bytes);
			return true;
		} else {
			return false;
		}
	}
	
	public static String getRaw(ByteBuffer buf) {
		int stringLength = VarIntType.getRaw(buf);
		byte[] bytes = new byte[stringLength];
		buf.get(bytes);
		return new String(bytes, StandardCharsets.UTF_8);
	}
	
	public static int getLengthRaw(ByteBuffer buf) {
		int varIntLength = VarIntType.getLengthRaw(buf);
		if (varIntLength == -1 || buf.remaining() < varIntLength) {
			return -1;
		}
		int pos = buf.position();
		int stringLength = VarIntType.getRaw(buf);
		buf.position(pos);
		return varIntLength + stringLength;
	}
	
	public static int stringToLength(String data) {
		return stringToLength(data.getBytes(StandardCharsets.UTF_8));
	}
	
	public static int stringToLength(byte[] data) {
		int varIntLength = VarIntType.intToLength(data.length);
		return varIntLength + data.length;
	}
	
	@Override
	public String get(ByteBuffer buf) {
		return getRaw(buf);
	}

	@Override
	public int getLength(ByteBuffer buf) {
		return getLengthRaw(buf);
	}

	@Override
	public boolean write(String data, ByteBuffer buf) {
		return writeRaw(data, buf);
	}

}
