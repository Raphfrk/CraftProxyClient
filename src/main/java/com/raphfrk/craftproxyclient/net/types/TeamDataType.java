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

public class TeamDataType extends Type<Object> {

	public boolean writeRaw(Object data, ByteBuffer buf) {
		throw new UnsupportedOperationException();
	}
	
	public static Object getRaw(ByteBuffer buf) {
		throw new UnsupportedOperationException();
	}
	
	public static int getLengthRaw(ByteBuffer buf) {
		if (buf.remaining() < 1) {
			return -1;
		}
		int pos = buf.position();
		try {
			byte mode = buf.get();
			int length = 1;
			int l;
			if (mode == 0 || mode == 2) {
				for (int i = 0; i < 2; i++) {
					l = String16Type.getLengthRaw(buf);
					if (l == -1) {
						return -1;
					}
					buf.position(buf.position() + l);
					length += l;
				}
				if (buf.remaining() == 0) {
					return -1;
				}
				buf.get();
				length += 1;
			}
			if (mode == 0 || mode == 3 || mode == 4) {
				if (buf.remaining() < 2) {
					return -1;
				}
				short count = buf.getShort();
				length += 2;
				for (int i = 0; i < count; i++) {
					l = String16Type.getLengthRaw(buf);
					if (l == -1) {
						return -1;
					}
					buf.position(buf.position() + l);
					length += l;
				}
			}
			return length;
		} finally {
			buf.position(pos);
		}
	}

	@Override
	public Object get(ByteBuffer buf) {
		return getRaw(buf);
	}

	@Override
	public int getLength(ByteBuffer buf) {
		return getLengthRaw(buf);
	}

	@Override
	public boolean write(Object data, ByteBuffer buf) {
		return writeRaw(data, buf);
	}

}
