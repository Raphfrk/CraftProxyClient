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

import java.io.EOFException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import com.raphfrk.craftproxyclient.net.protocol.Packet;
import com.raphfrk.craftproxyclient.net.protocol.PacketChannel;

public class TransferConnection extends Thread {
	
	private final PacketChannel in;
	private final PacketChannel out;
	private final ReentrantLock outLock = new ReentrantLock();
	private final ConcurrentLinkedQueue<Packet> sendQueue = new ConcurrentLinkedQueue<Packet>();
	private TransferConnection other;
	private AtomicBoolean running = new AtomicBoolean(true);
	
	public TransferConnection(PacketChannel in, PacketChannel out) {
		this(in, out, null);
	}
	
	public TransferConnection(PacketChannel in, PacketChannel out, TransferConnection other) {
		this.in = in;
		this.out = out;
		this.other = other;
	}
	
	public void run() {
		LinkedList<Integer> ids = new LinkedList<Integer>();
		while (!interrupted()) {
			try {
				if (!running.get()) {
					break;
				}
				ids.add(in.getPacketId());
				if (ids.size() > 100) {
					ids.removeFirst();
				}
				outLock.lock();
				try {
					in.transferPacket(out);
				} finally {
					outLock.unlock();
				}
				while (!sendQueue.isEmpty()) {
					outLock.lock();
					try {
						flushPacketQueue();
					} finally {
						outLock.lock();
					}
				}
				if (in.getPacketId() == 0xFF) {
					break;
				}
			} catch (IOException e) {
				if (!(e instanceof EOFException)) {
					/*e.printStackTrace();
					Iterator<Integer> i = ids.iterator();
					StringBuilder sb = new StringBuilder("Ids : ");
					while (i.hasNext()) {
						sb.append(i.next() + ", " );
					}
					System.out.println("Ids " + sb.toString());
					*/
				}
				break;
			}
		}
		if (other != null) {
			other.interrupt();
		}
	}
	
	public void queuePacket(Packet p) {
		sendQueue.add(p);
		if (outLock.tryLock()) {
			try {
				flushPacketQueue();
			} catch (IOException e) {
				e.printStackTrace();
				interrupt();
			} finally {
				outLock.unlock();
			}
		} else {
			
		}
	}
	
	public void interrupt() {
		running.set(false);
		super.interrupt();
	}
	
	private void flushPacketQueue() throws IOException {
		while (!sendQueue.isEmpty()) {
			out.writePacket(sendQueue.poll());
		}
	}

}
