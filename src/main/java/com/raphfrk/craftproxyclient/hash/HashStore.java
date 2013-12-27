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
package com.raphfrk.craftproxyclient.hash;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

import com.raphfrk.craftproxyclient.gui.CraftProxyGUI;
import com.raphfrk.craftproxyclient.io.HashFileStore;

public class HashStore {

	private final TLongObjectMap<Reference<Hash>> map = new TLongObjectHashMap<Reference<Hash>>();
	private final ReferenceQueue<Hash> refQueue = new ReferenceQueue<Hash>();
	private final Hash[] hardList = new Hash[2048];
	private int hardCount = 0;
	private final HashFileStore fileStore;

	public HashStore(File dirName, long capacity, CraftProxyGUI gui) throws IOException {
		fileStore = new HashFileStore(dirName, capacity, gui);
		fileStore.start();
	}

	public boolean hasKey(long hash) {
		processQueue();
		if (map.containsKey(hash)) {
			return true;
		}
		return fileStore.hasKey(hash);
	}
	
	public void init() throws IOException {
		fileStore.init();
	}
	
	public void shutdown() {
		fileStore.shutdown();
	}

	public void add(Hash hash) throws IOException {
		processQueue();
		try {
			fileStore.putHash(hash);
		} catch (InterruptedException e) {
			throw new IOException("Unable to add hash", e);
		}
		map.put(hash.getHash(), new KeySoftReference(hash, refQueue));
		hardList[(hardCount++) % hardList.length] = hash;
	}

	public Hash get(long hash) throws IOException {
		processQueue();
		Reference<Hash> ref = map.get(hash);
		Hash h = null;
		if (ref != null) {
			h = ref.get();
		}
		if (h != null) {
			return h;
		}
		Hash[] hashes = fileStore.readHash(hash);
		if (hashes == null) {
			System.out.println("Readback failed, map contains " + map.containsKey(hash));
			System.out.println("Readback failed, fileStore contains " + fileStore.hasKey(hash));
		}
		for (Hash hh : hashes) {
			if (hh.getHash() == hash) {
				h = hh;
			}
			add(hh);
		}
		return h;
	}

	private void processQueue() {
		KeySoftReference ref;
		while ((ref = (KeySoftReference) refQueue.poll()) != null) {
			map.remove(ref.getKey());
		}
	}
	
	private static class KeySoftReference extends SoftReference<Hash> {

		private long key;
		
		public KeySoftReference(Hash hash, ReferenceQueue<Hash> queue) {
			super(hash, queue);
			this.key = hash.getHash();
		}
		
		public long getKey() {
			return key;
		}
		
	}
	
}
