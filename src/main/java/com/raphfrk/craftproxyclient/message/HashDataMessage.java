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

import com.raphfrk.craftproxyclient.hash.Hash;
import com.raphfrk.craftproxyclient.net.protocol.CompressionManager;

public class HashDataMessage extends SubMessage {

	private final static int id = 2;

	private final Hash[] hashes;

	public static String getSubCommandRaw() {
		return "hashdata";
	}

	public HashDataMessage(Hash[] hashes) {
		this(hashes, 0, hashes.length);
	}

	public HashDataMessage(Hash[] hashes, int off, int len) {
		this.hashes = new Hash[len];
		System.arraycopy(hashes, off, this.hashes, 0, len);
	}
	
	public HashDataMessage(byte[] data) throws IOException {
		ByteBuffer buf = ByteBuffer.wrap(data);
		try {
			int inflatedLength = buf.getInt();
			byte[] inflated = new byte[inflatedLength];
			CompressionManager.inflate(data, 4, data.length - 4, inflated);
			buf = ByteBuffer.wrap(inflated);
			int count = buf.get();
			if (count < 0) {
				throw new IOException("Negative hash count " + count + " received");
			}
			this.hashes = new Hash[count];
			for (int i = 0; i < hashes.length; i++) {
				int len = buf.getShort();
				if (len < 0) {
					throw new IOException("Negative hash length " + len + " received");
				}
				byte[] hashData = new byte[len];
				buf.get(hashData);
				this.hashes[i] = new Hash(hashData);
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
		for (int i = 0; i < hashes.length; i++) {
			len += hashes[i].getLength();
		}
		len += 1; // count
		len += 2 * hashes.length; // hash length
		byte[] inflated = new byte[len];
		ByteBuffer buf = ByteBuffer.wrap(inflated);
		buf.put((byte) hashes.length);
		for (int i = 0; i < hashes.length; i++) {
			buf.putShort((short) hashes[i].getLength());
			hashes[i].put(buf);
		}
		if (buf.hasRemaining()) {
			throw new IllegalStateException("Hash data length calculation error");
		}
		byte[] output = new byte[inflated.length + 100];
		int size = CompressionManager.deflate(inflated, output);
		byte[] clipped = new byte[size + 4];
		System.arraycopy(output, 0, clipped, 4, clipped.length - 4);
		ByteBuffer.wrap(clipped).putInt(inflated.length);
		return clipped;
	}

	@Override
	public int getId() {
		return id;
	}
	
	public Hash[] getHashes() {
		return hashes;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof HashDataMessage)) {
			return false;
		} else {
			HashDataMessage h = (HashDataMessage) o;
			if (hashes.length != h.hashes.length) {
				return false;
			}
			for (int i = 0; i < hashes.length; i++) {
				if (hashes[i] != h.hashes[i] && (hashes[i] == null || !hashes[i].equals(h.hashes[i]))) {
					return false;
				}
			}
			return true;
		}
	}
}
