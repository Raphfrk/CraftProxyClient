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
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import com.raphfrk.craftproxyclient.net.protocol.Handshake;
import com.raphfrk.craftproxyclient.net.protocol.PacketChannel;
import com.raphfrk.craftproxyclient.net.protocol.PacketRegistry;
import com.raphfrk.craftproxyclient.net.protocol.p164bootstrap.P164BootstrapPacketRegistry;

public class ConnectionListener extends Thread {
	
	private final static int BUFFER_SIZE = 4 * 1024 * 1024;
	private final static int WRITE_BUFFER_SIZE = 1024 * 1024;
	
	private final AtomicBoolean running = new AtomicBoolean();
	private final ServerSocketChannel socket;
	private final InetSocketAddress serverAddr;
	
	private final P164BootstrapPacketRegistry p164Bootstrap = new P164BootstrapPacketRegistry();
	
	public ConnectionListener(int port, String serverHostname, int serverPort) throws IOException {
		serverAddr = new InetSocketAddress(serverHostname, serverPort);
		this.socket = ServerSocketChannel.open();
		this.socket.socket().bind(new InetSocketAddress(port));
		start();
	}
	
	public void run() {
		while (!interrupted()) {
			SocketChannel c;
			try {
				c = socket.accept();
			} catch (IOException e) {
				close();
				break;
			}
			try {
				PacketChannel clientPacketChannel = new PacketChannel(c, BUFFER_SIZE, WRITE_BUFFER_SIZE);
				try {
					int id = clientPacketChannel.getPacketId();
					PacketRegistry registry = null;
					Handshake handshake = null;

					if (id == 2) {
						// 1.64 protocol
						clientPacketChannel.setRegistry(p164Bootstrap);
						handshake = p164Bootstrap.getHandshake(clientPacketChannel.getPacket());
						registry = p164Bootstrap.getRegistry(handshake);
						if (registry == null) {
							continue;
						}
						clientPacketChannel.setRegistry(registry);
					}
					
					
					if (registry == null) {
						continue;
					}
					
					SocketChannel server = SocketChannel.open();
					server.connect(serverAddr);
					
					try {
						PacketChannel serverPacketChannel = new PacketChannel(server, BUFFER_SIZE, WRITE_BUFFER_SIZE);
						serverPacketChannel.setRegistry(registry);
						registry.handleLogin(handshake, clientPacketChannel, serverPacketChannel, serverAddr);
												
					} finally {
						server.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} finally {
				try {
					c.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	public void close() {
		if (running.compareAndSet(true, false)) {
			interrupt();
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}

}