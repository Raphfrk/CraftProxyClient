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

public class SpawnDataType extends Type<int[]> {

	public boolean writeRaw(int[] data, ByteBuffer buf) {
		if (buf.remaining() >= getLengthRaw(buf)) {
			buf.putInt(data[0]);
			if (data[0] != 0) {
				for (int i = 1; i < 4; i++) {
					buf.putShort((short) data[i]);
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	public static int[] getRaw(ByteBuffer buf) {
		int[] arr = new int[4];
		arr[0] = buf.getInt();
		if (arr[0] == 0) {
			for (int i = 1; i < 4; i++) {
				arr[i] = buf.getShort();
			}
		}
		return arr;
	}
	
	public static int getLengthRaw(ByteBuffer buf) {
		if (buf.remaining() < 4) {
			return -1;
		}
		int id = buf.getInt(buf.position());
		if (id == 0) {
			return 4;
		} else {
			return 10;
		}
	}
	
	@Override
	public int[] get(ByteBuffer buf) {
		return getRaw(buf);
	}

	@Override
	public int getLength(ByteBuffer buf) {
		return getLengthRaw(buf);
	}

	@Override
	public boolean write(int[] data, ByteBuffer buf) {
		return writeRaw(data, buf);
	}

}
