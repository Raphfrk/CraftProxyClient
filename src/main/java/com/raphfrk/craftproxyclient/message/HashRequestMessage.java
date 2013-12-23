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
package com.raphfrk.craftproxyclient.message;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class HashRequestMessage extends SubMessage {

	private final static int id = 1;

	private final long[] hashes;

	public static String getSubCommandRaw() {
		return "hashreq";
	}

	public HashRequestMessage(long[] hashes) {
		this(hashes, 0, hashes.length);
	}

	public HashRequestMessage(long[] hashes, int off, int len) {
		this.hashes = new long[len];
		System.arraycopy(hashes, off, this.hashes, 0, len);
	}
	
	public HashRequestMessage(byte[] data) throws IOException {
		ByteBuffer buf = ByteBuffer.wrap(data);
		try {
			int count = buf.get();
			if (count < 0) {
				throw new IOException("Negative hash count " + count + " received");
			}
			this.hashes = new long[count];
			for (int i = 0; i < hashes.length; i++) {
				this.hashes[i] = buf.getLong();
			}
		} catch (BufferUnderflowException e) {
			throw new IOException(e);
		}
	}

	@Override
	public String getSubCommand() {
		return getSubCommandRaw();
	}

	@Override
	public byte[] getData() {
		int len = 0;
		len += 1; // count
		len += 8 * hashes.length; // hash length
		byte[] ser = new byte[len];
		ByteBuffer buf = ByteBuffer.wrap(ser);
		buf.put((byte) hashes.length);
		for (int i = 0; i < hashes.length; i++) {
			buf.putLong(hashes[i]);
		}
		if (buf.hasRemaining()) {
			throw new IllegalStateException("Hash data length calculation error");
		}
		return buf.array();
	}

	@Override
	public int getId() {
		return id;
	}
	
	public long[] getHashes() {
		return hashes;
	}
}
