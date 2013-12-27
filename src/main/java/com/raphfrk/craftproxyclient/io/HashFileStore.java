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
package com.raphfrk.craftproxyclient.io;

import gnu.trove.impl.Constants;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.raphfrk.craftproxyclient.hash.Hash;

public class HashFileStore {
	
	private static final Comparator<File> fileComparator = new FileComparator();
	private static FileLock fileLock = null;
	
	private final File dir;
	private final String prefix;
	private final FileSizeUpdatable gui;
	private final long capacity;
	private final TLongIntMap FAT = new TLongIntHashMap(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0, -1);
	private final Lock fileSync = new ReentrantLock();
	private final WriteThread writeThread = new WriteThread();
	private final ConcurrentLinkedQueue<NotifyQueueLink> writeNotifyQueue = new ConcurrentLinkedQueue<NotifyQueueLink>();
	private final TLongObjectMap<Hash> RAMMap = new TLongObjectHashMap<Hash>();
	private final TLongSet addedSet = new TLongHashSet();
	private long used = 0;
	private boolean FATLoaded = false;
	private AtomicInteger nextId = new AtomicInteger();
	
	public HashFileStore(File dir, long capacity, FileSizeUpdatable gui) throws IOException {
		this(dir, capacity, gui, "CP-");
	}
	
	public HashFileStore(File dir, long capacity, FileSizeUpdatable gui, String prefix) throws IOException {
		this.gui = gui;
		this.dir = dir;
		this.prefix = prefix;
		this.capacity = capacity;
		createDirectory(dir);
		prune();
	}
	
	public static void lockDirectory(File dir) throws IOException {
		createDirectory(dir);
		fileLock = new RandomAccessFile(new File(dir, "lock"), "rw").getChannel().tryLock();
		if (fileLock == null) {
			throw new IOException("Unable to obtain lock on cache directory");
		}
	}
	
	private static void createDirectory(File dir) throws IOException {
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IOException("Unable to create cache directory");
		}
		if (!dir.isDirectory()) {
			throw new IOException("Cache directory is a standard file");
		}
	}
	
	private void updateFileSize() {
		fileSync.lock();
		try {
			gui.updateFileSize(used);
		} finally {
			fileSync.unlock();
		}
	}
	
	private void prune() throws IOException {
		fileSync.lock();
		try {
			File[] files = getSortedFiles(true);
			int used = 0;
			long remaining = capacity;
			int i;
			for (i = 0; i < files.length; i++) {
				long length = files[i].length();
				remaining -= length;
				if (remaining < 0) {
					break;
				}
				used += length;
			}
			for (; i< files.length; i++) {
				files[i].delete();
			}
			this.used = used;
		} finally {
			fileSync.unlock();
		}
		updateFileSize();
	}
	
	private void readFAT() throws IOException {
		fileSync.lock();
		try {
			File[] files = getSortedFiles(false);
			for (File f : files) {
				try {
					readFAT(f);
				} catch (IOException e) {
					f.delete();
				}
			}
		} finally {
			fileSync.unlock();
		}
	}

	private void readFAT(File f) throws IOException {
		fileSync.lock();
		try {
			FileInputStream fis = new FileInputStream(f);
			try {
				GZIPInputStream gis = new GZIPInputStream(fis);
				DataInputStream dis = new DataInputStream(gis);
				int entryCount = dis.readInt();
				long[] entries = new long[entryCount];
				for (int i = 0; i < entries.length; i++) {
					entries[i] = dis.readLong();
				}
				int id = getId(f);
				for (int i = 0; i < entries.length; i++) {
					FAT.put(entries[i], id);
				}
			} finally {
				if (fis != null) {
					fis.close();
				}
			}
		} finally {
			fileSync.unlock();
		}
	}
	
	public void init() throws IOException {
		if (!FATLoaded) {
			readFAT();
			FATLoaded = true;
		}
	}
	
	public void putHash(Hash hash) throws InterruptedException, IOException {
		processQueue();
		if (!addedSet.add(hash.getHash())) {
			return;
		}
		RAMMap.put(hash.getHash(), hash);
		writeThread.add(hash);
	}
	
	public boolean hasKey(long hash) {
		return RAMMap.containsKey(hash) || FAT.containsKey(hash);
	}
	
	public Hash[] readHash(long hash) throws IOException {
		processQueue();
		Hash RAMHash = RAMMap.get(hash);
		if (RAMHash != null) {
			Hash[] single = new Hash[1];
			single[0] = RAMHash;
			return single;
		}
		int fileId = FAT.get(hash);
		if (fileId == -1) {
			return null;
		}
		fileSync.lock();
		try {
			File f = getFile(fileId);
			if (!f.exists() || f.isDirectory()) {
				return null;
			}
			FileInputStream fis = new FileInputStream(f);
			try {
				GZIPInputStream gis = new GZIPInputStream(fis);
				DataInputStream dis = new DataInputStream(gis);
				int entryCount = dis.readInt();
				long[] entries = new long[entryCount];
				for (int i = 0; i < entries.length; i++) {
					entries[i] = dis.readLong();
				}
				byte[][] hashData = new byte[entryCount][];
				for (int i = 0; i < entries.length; i++) {
					int length = dis.readInt();
					byte[] data = new byte[length];
					dis.readFully(data);
					hashData[i] = data;
				}
				Hash[] hashes = new Hash[entries.length];
				for (int i = 0; i < entries.length; i++) {
					Hash h = new Hash(hashData[i]);
					if (h.getHash() != entries[i]) {
						throw new IOException("Hash of data does not match hash given in entry list");
					}
					hashes[i] = h;
				}
				return hashes;
			} finally {
				if (fis != null) {
					fis.close();
				}
			}
		} finally {
			fileSync.unlock();
		}
	}
	
	public void start() {
		writeThread.start();
	}
	
	public void shutdown() {
		writeThread.interrupt();
		boolean interrupted = false;
		while (writeThread.isAlive()) {
			try {
				writeThread.join();
			} catch (InterruptedException e) {
				interrupted = true;
			}
		}
		if (interrupted) {
			Thread.currentThread().interrupt();
		}
	}
	
	private void processQueue() {
		NotifyQueueLink link;
		while ((link = writeNotifyQueue.poll()) != null) {
			long[] hashes = link.getHashes();
			int id = link.getId();
			for (long l : hashes) {
				RAMMap.remove(l);
				FAT.put(l, id);
			}
		}
	}
	
	private File[] getSortedFiles(boolean reverse) throws IOException {
		fileSync.lock();
		try {
			File[] files = dir.listFiles();
			List<File> cpFiles = new ArrayList<File>(files.length);
			long highestId = 0;
			for (int i = 0; i < files.length; i++) {
				int id = getId(files[i]);
				if (id != -1) {
					cpFiles.add(files[i]);
					if ((id & 0xFFFFFFFFL) > highestId) {
						highestId = id;
					}
				}
			}
			if (getNextId(highestId) == -1) {
				throw new IOException("Exhausted filename space");
			}
			Collections.sort(cpFiles, fileComparator);
			if (reverse) {
				Collections.reverse(cpFiles);
			}
			return cpFiles.toArray(new File[0]);
		} finally {
			fileSync.unlock();
		}
	}
	
	private int getNextId() {
		return getNextId(0);
	}
	
	private int getNextId(long highestUsedId) {
		if (highestUsedId < 0 || highestUsedId >= 0xFFFFFFFFL) {
			throw new IllegalArgumentException("Highest Id must be 32 bits");
		}
		while (true) {
			int i = nextId.get();
			if (i == -1) {
				return -1;
			} else {
				long iLong = i & 0xFFFFFFFFL;
				if (iLong < highestUsedId) {
					iLong = highestUsedId;
				}
				int next = (int) (iLong + 1);
				if (nextId.compareAndSet(i, next)) {
					return next;
				}
			}
		}
	}
	
	private File getFile(int id) {
		StringBuilder sb = new StringBuilder(20);
		sb.append(prefix);
		String idString = Long.toString(id & 0xFFFFFFFFL);
		for (int i = 0; i < 10 - idString.length(); i++) {
			sb.append("0");
		}
		sb.append(idString);
		sb.append(".gz");
		return new File(dir, sb.toString());
	}
	
	private int getId(File f) {
		String name = f.getName();
		if (!name.startsWith(prefix) || !name.endsWith(".gz") || name.length() != prefix.length() + 13) {
			return -1;
		}
		String index = name.substring(prefix.length(), name.length() - 3);
		long id;
		try {
			id = Long.parseLong(index);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return -1;
		}
		if (id > 0xFFFFFFFEL || id < 0) {
			return -1;
		}
		return (int) id;
	}
	
	private static class FileComparator implements Comparator<File> {
		@Override
		public int compare(File file1, File file2) {
			return file1.getName().compareTo(file2.getName());
		}
	}
	
	private class WriteThread extends Thread {
		
		private final BlockingQueue<Hash> pending = new LinkedBlockingQueue<Hash>(512);
		private final ArrayList<Hash> writeQueue = new ArrayList<Hash>(512);
		private final AtomicBoolean running = new AtomicBoolean(true);
		
		public void add(Hash h) throws InterruptedException {
			if (running.get()) {
				pending.put(h);
			} else {
				throw new IllegalStateException("Hash added after write thread interrupted");
			}
		}
		
		public void interrupt() {
			running.set(false);
			super.interrupt();
		}

		public void run() {
			try {
				while (!interrupted()) {
					Hash h;
					while ((h = pending.take()) != null) {
						handleQueue(h);
					}
				}
			} catch (InterruptedException e) {
			} finally {
				Hash h;
				while ((h = pending.poll()) != null) {
					handleQueue(h);
				}
				if (writeQueue.size() > 0) {
					writeFile();
				}
			}
		}
		
		private void handleQueue(Hash h) {
			writeQueue.add(h);
			if (writeQueue.size() == 512) {
				writeFile();
			}
		}
		
		private void writeFile() {
			int id = getNextId();
			File f = getFile(id);
			try {
				fileSync.lock();
				try {
					FileOutputStream fos = new FileOutputStream(f);
					try {
						GZIPOutputStream gos = new GZIPOutputStream(fos);
						DataOutputStream dos = new DataOutputStream(gos);
						dos.writeInt(writeQueue.size());
						for (Hash h : writeQueue) {
							dos.writeLong(h.getHash());
						}
						for (Hash h : writeQueue) {
							dos.writeInt(h.getLength());
							h.put(dos);
						}
						dos.flush();
						dos.close();
						long[] hashes = new long[writeQueue.size()];
						for (int i = 0; i < hashes.length; i++) {
							hashes[i] = writeQueue.get(i).getHash();
						}
						writeQueue.clear();
						writeNotifyQueue.add(new NotifyQueueLink(id, hashes));
					} finally {
						fos.close();
					}
					if (f.exists()) {
						used += f.length();
						
					}
				} finally {
					fileSync.unlock();
					updateFileSize();
				}
			} catch (IOException e) {
				Thread.currentThread().interrupt();
			}
		}

	}
	
	private static class NotifyQueueLink {
		
		private final int id;
		private final long[] hashes;
		
		public NotifyQueueLink(int id, long[] hashes) {
			this.id = id;
			this.hashes = hashes;
		}
		
		public int getId() {
			return id;
		}
		
		public long[] getHashes() {
			return hashes;
		}
	}

}
