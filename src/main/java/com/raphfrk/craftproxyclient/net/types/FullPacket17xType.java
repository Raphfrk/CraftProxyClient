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

public class FullPacket17xType extends Type<byte[]> {
	
	public FullPacket17xType() {
	}

	public static int getLengthRaw(ByteBuffer buf) {
		int lengthLength = VarIntType.getLengthRaw(buf);
		if (buf.remaining() < lengthLength) {
			return -1;
		}
		int pos = buf.position();
		try {
			int length = VarIntType.getRaw(buf);
			return length + lengthLength;
		} finally {
			buf.position(pos);
		}
	}
	
	@Override
	public boolean write(byte[] data, ByteBuffer buf) {
		int lengthLength = VarIntType.intToLength(data.length);
		if (buf.remaining() < data.length + lengthLength) {
			return false;
		}
		VarIntType.writeRaw(data.length, buf);
		buf.put(data);
		return true;
	}

	@Override
	public byte[] get(ByteBuffer buf) {
		int length = VarIntType.getRaw(buf);
		byte[] arr = new byte[length];
		buf.get(arr);
		return arr;
	}

	@Override
	public int getLength(ByteBuffer buf) {
		return getLengthRaw(buf);
	}

}
