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

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import com.raphfrk.craftproxyclient.message.InitMessage;
import com.raphfrk.craftproxyclient.message.MessageManager;
import com.raphfrk.craftproxyclient.net.protocol.Packet;
import com.raphfrk.craftproxyclient.net.protocol.PacketChannel;
import com.raphfrk.craftproxyclient.net.protocol.Protocol;

public class TransferConnection extends Thread {
	
	private final PacketChannel in;
	private final PacketChannel out;
	private final Protocol protocol;
	private final ReentrantLock outLock = new ReentrantLock();
	private final ConcurrentLinkedQueue<Packet> sendQueue = new ConcurrentLinkedQueue<Packet>();
	private TransferConnection other;
	private AtomicBoolean running = new AtomicBoolean(true);
	
	public TransferConnection(Protocol protocol, PacketChannel in, PacketChannel out) {
		this.protocol = protocol;
		this.in = in;
		this.out = out;
	}
	
	public void setOther(TransferConnection other) {
		this.other = other;
	}
	
	public void run() {
		LinkedList<Integer> ids = new LinkedList<Integer>();
		while (!interrupted()) {
			try {
				if (!running.get()) {
					break;
				}
				int id = in.getPacketId();
				ids.add(id);
				if (ids.size() > 100) {
					ids.removeFirst();
				}
				if (protocol.isMessagePacket(id)) {
					in.mark();
					Packet p = in.getPacket();
					String channel = protocol.getMessageChannel(p);
					if ("REGISTER".equals(channel)) {
						byte[] data = protocol.getMessageData(p);
						String[] channels = MessageManager.splitRegisterData(data);
						for (String c : channels) {
							if (MessageManager.getChannelName().equals(c)) {
								other.queuePacket(protocol.getRegisterPacket(MessageManager.getChannelName()));
								other.queuePacket(protocol.getSubMessage(new InitMessage()));
							}
						}
					}
					System.out.println("Received message " + p.getField(1));
					in.reset();
					in.discard();
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
				break;
			}
		}
	}
	
	public void queuePacket(Packet p) {
		sendQueue.add(p);
		if (outLock.tryLock()) {
			try {
				flushPacketQueue();
			} catch (IOException e) {
				running.set(false);
			} finally {
				outLock.unlock();
			}
		} else {
			
		}
	}
	
	public void interrupt() {
		System.out.println("transfer connection interrupted");
		queuePacket(protocol.getKick("Proxy server halted"));
		running.set(false);
	}
	
	private void flushPacketQueue() throws IOException {
		while (!sendQueue.isEmpty()) {
			out.writePacket(sendQueue.poll());
		}
	}

}
