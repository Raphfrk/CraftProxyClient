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
package com.raphfrk.craftproxyclient.net;

import gnu.trove.set.TLongSet;
import gnu.trove.set.TShortSet;
import gnu.trove.set.hash.TLongHashSet;
import gnu.trove.set.hash.TShortHashSet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.raphfrk.craftproxyclient.hash.Hash;
import com.raphfrk.craftproxyclient.hash.HashStore;
import com.raphfrk.craftproxyclient.message.MessageManager;

public class ConnectionManager {
	
	private final TLongHashSet hashes = new TLongHashSet();
	private final TLongHashSet newHashes = new TLongHashSet();
	private final TLongSet unknowns = new TLongHashSet();
	private final TLongSet requested = new TLongHashSet();
	private final TShortSet sectionIds = new TShortHashSet();
	private final HashStore store = new HashStore();
	
	public boolean addHash(Hash hash) {
		store.add(hash);
		unknowns.remove(hash.getHash());
		return unknowns.isEmpty();
	}
	
	public boolean hasUnknowns() {
		return !unknowns.isEmpty();
	}
	
	public int getUnknownCount() {
		return unknowns.size();
	}
	
	public long[] getUnknowns() {
		return unknowns.toArray();
	}
	
	public short[] getSectionIds() {
		return sectionIds.toArray();
	}
	
	public byte[] process(byte[] data) throws IOException {
		ByteBuffer buf = ByteBuffer.wrap(data);

		unknowns.clear();
		sectionIds.clear();
		newHashes.clear();
		List<byte[]> sections = new ArrayList<byte[]>();
		int len = 0;
		boolean success = true;
		while (buf.hasRemaining()) {
			byte[] section = decodeSection(buf, success);
			if (section == null) {
				success = false;
			} else {
				len += section.length;
				sections.add(section);
			}
		}
		if (!success) {
			return null;
		}
		byte[] decoded = new byte[len];
		int pos = 0;
		for (byte[] section : sections) {
			System.arraycopy(section, 0, decoded, pos, section.length);
			pos += section.length;
		}

		return decoded;
		
	}
	
	private byte[] decodeSection(ByteBuffer buf, boolean success) throws IOException {
		int magic = buf.getInt();
		if (magic != MessageManager.getMagicInt()) {
			throw new IOException("Incorrect magic pattern when decoding cached data " + Integer.toHexString(magic));
		}

		short sectionId = buf.getShort();
		
		int length = buf.getInt();
		
		int hashCount = buf.get() & 0xFF;
		
		long[] hashes = new long[hashCount];
		
		for (int i = 0; i < hashCount; i++) {
			long hash = getHash(buf);
			if (!store.hasKey(hash)) {
				if (requested.add(hash)) {
					unknowns.add(hash);
				}
			}
			hashes[i] = hash;
		}
		
		long sectionCRC = buf.getLong();
		
		if (unknowns.size() > 0) {
			return null;
		}
		
		if (!success) {
			// no point in decoding
			return null;
		}
		
		sectionIds.add(sectionId);

		byte[] section = new byte[length];

		int pos = 0;
		for (int i = 0; i < hashes.length; i++) {
			Hash h = store.get(hashes[i]);

			int len = h.copy(section, pos);

			if (len == -1) {
				throw new IOException("Section length error");
			}
			pos += len;
		}
		
		long crc = Hash.hash(section);
		
		if (crc != sectionCRC) {
			throw new IOException("Section CRC mismatch with cache");
		}
		
		return section;
		
	}
	
	private long getHash(ByteBuffer buf) throws IOException {
		byte c = buf.get();
		if (c == 0) {
			return buf.getLong();
		} else {
			throw new IOException("Unable to decode control byte " + c);
		}
	}

}
