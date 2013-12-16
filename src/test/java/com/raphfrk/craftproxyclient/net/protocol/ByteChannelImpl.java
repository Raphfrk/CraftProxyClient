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
import java.nio.channels.ByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class ByteChannelImpl implements ByteChannel {

	private final ReadableByteChannel readChannel;
	private final WritableByteChannel writeChannel;
	
	public ByteChannelImpl(ReadableByteChannel readChannel, WritableByteChannel writeChannel) {
		this.readChannel = readChannel;
		this.writeChannel = writeChannel;
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		return readChannel.read(dst);
	}

	@Override
	public void close() throws IOException {
		try {
			readChannel.close();
		} finally {
			writeChannel.close();
		}
	}

	@Override
	public boolean isOpen() {
		return readChannel.isOpen() || writeChannel.isOpen();
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		return writeChannel.write(src);
	}
	
}
