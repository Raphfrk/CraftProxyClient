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

public class CountSizedArrayType extends Type<byte[]> {
	
	private final NumberType countType;
	private final int elementLength;
	
	public CountSizedArrayType(NumberType countType, int elementLength) {
		this.countType = countType;
		this.elementLength = elementLength;
	}

	@Override
	public byte[] get(ByteBuffer buf) {
		int length = countType.getValue(buf);
		byte[] data = new byte[length * elementLength];
		buf.get(data);
		return data;
	}

	@Override
	public int getLength(ByteBuffer buf) {
		int pos = buf.position();
		try {
			int countLength = countType.getLength(buf);
			if (countLength == -1) {
				return -1;
			}
			if (buf.remaining() < countLength) {
				return -1;
			}
			int length = countType.getValue(buf);
			int dataLength = length * elementLength;
			if (buf.remaining() < dataLength) {
				return -1;
			}
			return countLength + dataLength;
		} finally {
			buf.position(pos);
		}
	}

	@Override
	public boolean write(byte[] data, ByteBuffer buf) {
		if (buf.remaining() >= getLength(buf)) {
			int elements = data.length / elementLength;
			countType.putValue(elements, buf);
			buf.put(data);
			return true;
		} else {
			return false;
		}
	}

}
