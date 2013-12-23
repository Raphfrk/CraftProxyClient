package com.raphfrk.craftproxyclient.hash;

import java.nio.ByteBuffer;

public class Hash {
	
	public static int HASH_LENGTH = 2048;
	
	public static int getHashCount(byte[] data) {
		return (data.length + HASH_LENGTH - 1) / HASH_LENGTH;
	}

	public static int getHashLength() {
		return HASH_LENGTH;
	}

	public static long hash(byte[] data) {
		return hash(data, 0, data.length);
	}
	
	public static long hash(byte[] data, int off, int len) {
		long h = len + len << 16;
		for (byte b : data) {
			h += Long.rotateRight(h, 5) + (b & 0xFF);
		}
		return h;
	}
	
	private final long hash;
	private final byte[] data;
	
	public Hash(byte[] data) {
		this(data, 0, data.length);
	}
	
	public Hash(byte[] data, int off, int len) {
		this.data = new byte[len];
		System.arraycopy(data, off, this.data, 0, len);
		this.hash = hash(this.data);
	}
	
	public long getHash() {
		return hash;
	}
	
	public int getLength() {
		return data.length;
	}
	
	public int copy(byte[] dst, int off) {
		if (off + data.length > dst.length) {
			return -1;
		}
		System.arraycopy(data, 0, dst, off, data.length);
		return data.length;
	}
	
	public void put(ByteBuffer buf) {
		buf.put(data);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Hash{");
		sb.append("hash = " + hash + ", ");
		sb.append("length = " + data.length + ", ");
		int i = 0;
		for (; i < data.length && i < 3; i++) {
			sb.append(data[i] + ", ");
		}
		sb.append("...");
		for (i = Math.max(i, data.length - 3); i < data.length; i++) {
			sb.append(data[i] + ", ");
		}
		sb.append("}");
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		return (int) hash;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof Hash)) {
			return false;
		} else {
			Hash h = (Hash) o;
			if (hash != h.hash) {
				return false;
			}
			if (data.length != h.data.length) {
				return false;
			}
			for (int i = 0; i < data.length; i++) {
				if (data[i] != h.data[i]) {
					return false;
				}
			}
			return true;
		}
	}

}
