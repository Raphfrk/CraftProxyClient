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

public class IntType extends FixedSizeType<Integer> {
	
	public IntType() {
		super(4);
	}

	public boolean writeRaw(int data, ByteBuffer buf) {
		if (buf.remaining() >= getFixedSize()) {
			putByte(data >> 24, buf);
			putByte(data >> 16, buf);
			putByte(data >> 8, buf);
			putByte(data >> 0, buf);
			return true;
		} else {
			return false;
		}
	}
	
	public static int getRaw(ByteBuffer buf) {
		int x = 0;
		x |= getByte(buf) << 24;
		x |= getByte(buf) << 16;
		x |= getByte(buf) << 8;
		x |= getByte(buf) << 0;
		return x;
	}
	
	@Override
	public boolean write(Integer data, ByteBuffer buf) {
		return writeRaw(data, buf);
	}

	@Override
	public Integer get(ByteBuffer buf) {
		return getRaw(buf);
	}

}
