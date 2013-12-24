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
import java.nio.channels.AsynchronousCloseException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import com.raphfrk.craftproxyclient.handler.HandlerManager;
import com.raphfrk.craftproxyclient.message.HashDataMessage;
import com.raphfrk.craftproxyclient.message.HashRequestMessage;
import com.raphfrk.craftproxyclient.message.InitMessage;
import com.raphfrk.craftproxyclient.message.MessageManager;
import com.raphfrk.craftproxyclient.message.SectionAckMessage;
import com.raphfrk.craftproxyclient.message.SubMessage;
import com.raphfrk.craftproxyclient.net.protocol.Packet;
import com.raphfrk.craftproxyclient.net.protocol.PacketChannel;
import com.raphfrk.craftproxyclient.net.protocol.Protocol;

public class TransferConnection extends Thread {
	
	private final PacketChannel in;
	private final PacketChannel out;
	private final Protocol protocol;
	private final ReentrantLock outLock = new ReentrantLock();
	private final ConcurrentLinkedQueue<Packet> sendQueue = new ConcurrentLinkedQueue<Packet>();
	private final ConnectionManager manager;
	private TransferConnection other;
	private final ConnectionListener parent;
	private AtomicBoolean running = new AtomicBoolean(true);
	private boolean caching = false;
	
	public TransferConnection(String type, Protocol protocol, PacketChannel in, PacketChannel out, ConnectionManager manager, ConnectionListener parent) {
		super("TransferConnection " + type);
		this.protocol = protocol;
		this.in = in;
		this.out = out;
		this.parent = parent;
		this.manager = manager;
	}
	
	public void setOther(TransferConnection other) {
		this.other = other;
	}
	
	public void run() {
		int lastUsage = 0;
		long lastUpdateTime = 0;
		LinkedList<Integer> ids = new LinkedList<Integer>();
		while (!interrupted()) {
			try {
				if (!running.get()) {
					break;
				}
				int bandwidth = in.getBandwidthUsage();
				if (bandwidth > 1024 + lastUsage && System.currentTimeMillis() > 250 + lastUpdateTime) {
					lastUsage = bandwidth;
					lastUpdateTime = System.currentTimeMillis();
					parent.updateGUIBandwidth();
				}
				int id = in.getPacketId();
				ids.add(id);
				if (ids.size() > 100) {
					ids.removeFirst();
				}
				if (protocol.isMessagePacket(id)) {
					Packet p = in.getPacket();
					String channel = protocol.getMessageChannel(p);
					if ("REGISTER".equals(channel)) {
						byte[] data = protocol.getMessageData(p);
						String[] channels = MessageManager.splitRegisterData(data);
						for (String c : channels) {
							if (MessageManager.getChannelName().equals(c)) {
								other.queuePacket(protocol.getRegisterPacket(MessageManager.getChannelName()));
								other.queuePacket(protocol.convertSubMessageToPacket(new InitMessage()));
							}
						}
						out.writePacketLocked(p, outLock);
					} else if (MessageManager.getChannelName().equals(channel)) {
						SubMessage subMessage = protocol.convertPacketToSubMessage(p);
						HandlerManager.handle(this, subMessage);
					} else {
						out.writePacketLocked(p, outLock);
					}
				} else if (caching && manager != null && protocol.isDataPacket(id)) {
					Packet p = in.getPacket();
					byte[] data = protocol.getDataArray(p);
					data = manager.process(data);
					if (data == null) {
						in.mark();
						try {
							int pos = 0;
							long[] unknowns = manager.getUnknowns();
							while (pos < unknowns.length) {
								int len = Math.min(64, unknowns.length - pos);
								HashRequestMessage request = new HashRequestMessage(unknowns, pos, len);
								other.queuePacket(protocol.convertSubMessageToPacket(request));
								pos += len;
							}
							while (manager.hasUnknowns()) {
								int id2 = in.getPacketId();
								if (protocol.isMessagePacket(id2)) {
									Packet p2 = in.getPacket();
									String channel = protocol.getMessageChannel(p2);
									if (MessageManager.getChannelName().equals(channel)) {
										SubMessage subMessage = protocol.convertPacketToSubMessage(p2);
										if (subMessage instanceof HashDataMessage) {
											HashDataMessage dm = (HashDataMessage) subMessage;
											HandlerManager.handle(this, subMessage);
										}
									}
								} else {
									in.skipPacket();
								}
							}
						} finally {
							in.reset();
							in.discard();
						}
					}
					data = protocol.getDataArray(p);
					data = manager.process(data);
					if (data == null) {
						throw new IOException("Unable to process packet even after all unknowns were filled");
					}
					protocol.setDataArray(p, data);
					out.writePacketLocked(p, outLock);
					other.queuePacket(protocol.convertSubMessageToPacket(new SectionAckMessage(manager.getSectionIds())));
				} else {
					in.transferPacketLocked(out, outLock);
				}
				while (!sendQueue.isEmpty()) {
					outLock.lock();
					try {
						flushPacketQueue();
					} finally {
						outLock.lock();
					}
				}
				if (protocol.isKickMessage(in.getPacketId())) {
					in.transferPacketLocked(out, outLock);
					break;
				}
			} catch (AsynchronousCloseException e) {
				break;
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
	
	private void flushPacketQueue() throws IOException {
		while (!sendQueue.isEmpty()) {
			out.writePacket(sendQueue.poll());
		}
	}
	
	public ConnectionManager getManager() {
		return manager;
	}
	
	public void setCaching() {
		caching = true;
	}

}
