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

public class ByteSizedByteArrayType extends Type<byte[]> {

	public boolean writeRaw(byte[] data, ByteBuffer buf) {
		if (buf.remaining() >= data.length + 1) {
			buf.put((byte) data.length);
			buf.put(data);
			return true;
		} else {
			return false;
		}
	}
	
	public static byte[] getRaw(ByteBuffer buf) {
		byte[] arr = new byte[getLengthRaw(buf) - 1];
		buf.get();
		buf.get(arr);
		return arr;
	}
	
	public static int getLengthRaw(ByteBuffer buf) {
		if (buf.remaining() == 0) {
			return -1;
		}
		return 1 + (buf.get(buf.position()) & 0xFF);
	}
	
	@Override
	public byte[] get(ByteBuffer buf) {
		return getRaw(buf);
	}

	@Override
	public int getLength(ByteBuffer buf) {
		return getLengthRaw(buf);
	}

	@Override
	public boolean write(byte[] data, ByteBuffer buf) {
		return writeRaw(data, buf);
	}

}
