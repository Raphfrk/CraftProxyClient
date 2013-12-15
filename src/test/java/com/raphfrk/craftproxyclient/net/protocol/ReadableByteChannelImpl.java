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
package com.raphfrk.craftproxyclient.net.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Random;

public class ReadableByteChannelImpl implements ReadableByteChannel {
	
	private final byte[] arr;
	private int i;
	
	public ReadableByteChannelImpl(byte[] arr, int pos) {
		this.arr = new byte[pos];
		for (int i = 0; i < pos; i++) {
			this.arr[i] = arr[i];
		}
		this.i = 0;
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public boolean isOpen() {
		return true;
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		
		if (i == arr.length) {
			return -1;
		}
		
		int r = new Random().nextInt(20) + 1;
		if (r > dst.remaining()) {
			r = dst.remaining();
		}
		if (r + i > arr.length) {
			r = arr.length - i;
		}
		dst.put(arr, i, r);
		i += r;
		return r;
	}

}
