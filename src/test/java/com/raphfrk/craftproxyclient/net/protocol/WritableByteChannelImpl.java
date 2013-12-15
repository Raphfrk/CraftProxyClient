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
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class WritableByteChannelImpl implements WritableByteChannel {
	
	private final ArrayList<Byte> arr;
	
	public WritableByteChannelImpl() {
		this.arr = new ArrayList<Byte>();
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public boolean isOpen() {
		return true;
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		int w = new Random().nextInt(20);
		if (w > src.remaining()) {
			w = src.remaining();
		}
		for (int i = 0; i < w; i++) {
			arr.add(src.get());
		}
		return w;
	}
	
	public byte[] toByteArray() {
		byte[] dst = new byte[arr.size()];
		Iterator<Byte> itr = arr.iterator();
		int i = 0;
		while (itr.hasNext()) {
			dst[i++] = itr.next();
		}
		return dst;
	}

}
