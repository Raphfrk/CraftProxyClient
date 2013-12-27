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
package com.raphfrk.craftproxyclient.hash.tree;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.TreeSet;

public class HashTreeSet {
	
	private static long[] lengthMasks;
	
	static {
		lengthMasks = new long[8];
		long l = 0xFFL << 56;
		for (int i = 0; i < lengthMasks.length; i++) {
			lengthMasks[i] = l;
			l = l >> 8;
		}
	}
	
	private final TreeSet<Long> tree;
	
	public HashTreeSet() {
		this.tree = new TreeSet<Long>();
	}
	
	protected boolean add(long value) {
		return tree.add(value);
	}
	
	protected int getSize(long value) {
		if (!tree.contains(value)) {
			return -1;
		}
		Long next = value < Long.MAX_VALUE ? tree.ceiling(value + 1) : null;
		Long prev = value > Long.MIN_VALUE ? tree.floor(value - 1) : null;
		
		int i = 0;
		if (prev != null) {
			for (i = 0; i < lengthMasks.length; i++) {
				if (((prev ^ value) & lengthMasks[i]) != 0) {
					break;
				}
			}
		}
		if (next != null) {
			for (; i < lengthMasks.length; i++) {
				if (((next ^ value) & lengthMasks[i]) != 0) {
					break;
				}
			}
		}
		return i + 1;
	}
	
	public boolean writeHash(ByteBuffer buf, long hash) {
		int size = getSize(hash);
		if (size == -1) {
			if (buf.remaining() < 9) {
				return false;
			}
			add(hash);
			buf.put((byte) 0);
			buf.putLong(hash);
			return true;
		}
		int prefix = (int) (hash >>> 56);
		if ((prefix & 0xFC) == 0) {
			if (buf.remaining() < size + 1) {
				return false;
			}
			buf.put((byte) 1);
		} else if (buf.remaining() < size) {
			return false;
		}
		int shift = 56;
		for (int i = 0; i < size; i++) {
			byte b = (byte) (hash >> shift);
			buf.put(b);
			shift -= 8;
		}
		return true;
	}
	
	public long readHash(ByteBuffer buf) throws IOException {
		if (!buf.hasRemaining()) {
			throw new IOException();
		}
		int pos = buf.position();
		byte control = buf.get();

		if (control == 0) {
			if (buf.remaining() < 8) {
				buf.position(pos);
				throw new IOException();
			}
			long h = buf.getLong();
			add(h);
			return h;
		} else if (control == 1) {
			if (!buf.hasRemaining()) {
				buf.position(pos);
				throw new IOException();
			}
			control = buf.get();
		} else if (control == 2) {
			buf.position(pos);
			throw new IOException("Unexpected magic pattern start");
		}
		buf.position(buf.position() - 1);
		long key = 0;
		int shift = 56;
		for (int i = 0; i < lengthMasks.length; i++) {
			if (!buf.hasRemaining()) {
				buf.position(pos);
				throw new IOException();
			}
			long nextByte = (buf.get() & 0xFFL) << shift;
			key |= nextByte;
			long mask = lengthMasks[i];
			long keyHigh = key | (~mask);
			Long top = tree.floor(keyHigh);
			Long bottom = tree.ceiling(key);
			if (top != null && top == bottom) {
				add(top);
				return top;
			}
			shift -= 8;
		}
		throw new IOException("Unable to find hash");
	}
	
}
