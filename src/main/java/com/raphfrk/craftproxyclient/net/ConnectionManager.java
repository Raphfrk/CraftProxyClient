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

import gnu.trove.list.TIntList;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.set.TLongSet;
import gnu.trove.set.TShortSet;
import gnu.trove.set.hash.TLongHashSet;
import gnu.trove.set.hash.TShortHashSet;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.raphfrk.craftproxyclient.hash.Hash;
import com.raphfrk.craftproxyclient.hash.HashStore;
import com.raphfrk.craftproxyclient.hash.tree.HashTreeSet;
import com.raphfrk.craftproxyclient.message.MessageManager;

public class ConnectionManager {
	
	private final TLongSet unknowns = new TLongHashSet();
	private final TLongSet requested = new TLongHashSet();
	private final TShortSet sectionIds = new TShortHashSet();
	private final HashStore store = new HashStore();
	private final List<long[]> sectionHashes = new ArrayList<long[]>();
	private final TLongList sectionCRCs = new TLongArrayList();
	private final TIntList sectionLengths = new TIntArrayList();
	private final HashTreeSet hashSet = new HashTreeSet();
	private int decodedLength = 0;
	
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
	
	public boolean scanHashes(byte[] data) throws IOException {
		
		ByteBuffer buf = ByteBuffer.wrap(data);

		unknowns.clear();
		sectionIds.clear();

		sectionHashes.clear();
		sectionCRCs.clear();
		sectionLengths.clear();
		
		decodedLength = 0;

		while (buf.hasRemaining()) {
			decodedLength += getSectionHashes(buf);
		}
		
		return unknowns.isEmpty();
		
	}
	
	public byte[] process() throws IOException {

		byte[] decoded = new byte[decodedLength];
		ByteBuffer buf = ByteBuffer.wrap(decoded);
		
		int sectionCount = sectionHashes.size();
		for (int i = 0; i < sectionCount; i++) {
			writeSection(buf, i);
		}

		return decoded;
		
	}
	
	private int getSectionHashes(ByteBuffer buf) throws IOException {
		int magic = buf.getInt();
		if (magic != MessageManager.getMagicInt()) {
			throw new IOException("Incorrect magic pattern when decoding cached data " + Integer.toHexString(magic));
		}

		short sectionId = buf.getShort();
		
		int length = buf.getInt();
		
		int hashCount = buf.get() & 0xFF;
		
		long[] hashes = new long[hashCount];
		
		for (int i = 0; i < hashCount; i++) {
			long hash = hashSet.readHash(buf);
			if (!store.hasKey(hash)) {
				if (requested.add(hash)) {
					unknowns.add(hash);
				}
			}
			hashes[i] = hash;
		}
		
		long sectionCRC = buf.getLong();
		
		sectionHashes.add(hashes);
		sectionCRCs.add(sectionCRC);
		sectionLengths.add(length);
		sectionIds.add(sectionId);
		
		return length; 
	}
	
	public void writeSection(ByteBuffer buf, int sectionIndex) throws IOException {
		
		if (unknowns.size() > 0) {
			throw new IOException("All unknowns were not found");
		}

		long[] hashes = sectionHashes.get(sectionIndex);
		
		int start = buf.position();
		
		for (int i = 0; i < hashes.length; i++) {
			Hash h = store.get(hashes[i]);
			if (h == null) {
				throw new IOException("Unable to find hash "+ hashes[i]);
			}
			try {
				h.put(buf);
			} catch (BufferOverflowException e) {
				throw new IOException("Unable write hash "+ hashes[i], e);
			}
		}
		
		int end = buf.position();
		
		if (end - start != sectionLengths.get(sectionIndex)) {
			throw new IOException("Unexpected section length " + (end - start));
		}
		
		long crc = Hash.hash(buf.array(), start, end - start);
		
		if (crc != sectionCRCs.get(sectionIndex)) {
			throw new IOException("Section CRC mismatch with cache " + crc + " " + sectionCRCs.get(sectionIndex));
		}		
	}
	
}
