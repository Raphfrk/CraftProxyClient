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
	
	public boolean add(long value) {
		return tree.add(value);
	}
	
	public int getSize(long value) {
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
