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

import com.raphfrk.craftproxyclient.gui.CraftProxyGUI;
import com.raphfrk.craftproxyclient.net.protocol.Handshake;
import com.raphfrk.craftproxyclient.net.protocol.PacketChannel;
import com.raphfrk.craftproxyclient.net.protocol.Protocol;
import com.raphfrk.craftproxyclient.net.protocol.p164bootstrap.P164BootstrapProtocol;

public class ConnectionListener extends Thread {
	
	private final static int BUFFER_SIZE = 4 * 1024 * 1024;
	private final static int WRITE_BUFFER_SIZE = 1024 * 1024;
	
	private final ServerSocketChannel socket;
	private final InetSocketAddress serverAddr;
	private final InetSocketAddress localAddr;
	private final CraftProxyGUI gui;
	
	private final P164BootstrapProtocol p164Bootstrap = new P164BootstrapProtocol();
	
	public ConnectionListener(CraftProxyGUI gui, int port, String serverHostname, int serverPort) throws IOException {
		this.gui = gui;
		this.serverAddr = new InetSocketAddress(serverHostname, serverPort);
		this.localAddr = new InetSocketAddress(port);
		this.socket = ServerSocketChannel.open();
		this.socket.socket().bind(localAddr);
	}
	
	public void run() {
		TransferConnection serverToClient = null;
		TransferConnection clientToServer = null;
		try {
			gui.setStatus("<html>Waiting for connection<br>localhost:" + localAddr.getPort() + "</html>");
			SocketChannel c;
			try {
				c = socket.accept();
			} catch (IOException e) {
				return;
			}
			Protocol protocol = null;
			PacketChannel client = null;
			try {
				client = new PacketChannel(c, BUFFER_SIZE, WRITE_BUFFER_SIZE);
				try {
					int id = client.getPacketId();
					Handshake handshake = null;

					if (id == 2) {
						// 1.64 protocol
						client.setRegistry(p164Bootstrap.getPacketRegistry());
						handshake = p164Bootstrap.getHandshake(client.getPacket());
						protocol = p164Bootstrap.getProtocol(handshake);
						if (protocol == null) {
							return;
						}
						client.setRegistry(protocol.getPacketRegistry());
					}


					if (protocol == null) {
						return;
					}
					
					gui.setStatus("<html>Connection received, login started<br>Protocol: " + protocol.getName() + "</html>");

					SocketChannel s = SocketChannel.open();
					s.connect(serverAddr);

					PacketChannel server = null;
					try {
						server = new PacketChannel(s, BUFFER_SIZE, WRITE_BUFFER_SIZE);
						server.setRegistry(protocol.getPacketRegistry());
						protocol.handleLogin(handshake, client, server, serverAddr);
					} catch (IOException e) {
						if (server != null) {
							protocol.sendKick("Connection closing " + e.getMessage(), client);
							s.close();
							return;
						}
					}
					
					gui.setStatus("<html>Login completed<br>Protocol: " + protocol.getName() + "</html>");

					try {
						serverToClient = new TransferConnection(protocol, server, client);
						clientToServer = new TransferConnection(protocol, client, server);

						serverToClient.start();
						clientToServer.start();

						serverToClient.join();
						clientToServer.join();
					} catch (InterruptedException e) {
					} finally {
						serverToClient.queuePacket(protocol.getKick("Proxy halted"));
						s.close();
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
		} finally {
			while (serverToClient != null && serverToClient.isAlive()) {
				try {
					serverToClient.join();
				} catch (InterruptedException e) {
				}
			}
			while (clientToServer != null && clientToServer.isAlive()) {
				try {
					clientToServer.join();
				} catch (InterruptedException e) {
				}
			}
			try {
				socket.close();
			} catch (IOException e) {
			}
			gui.setDone();
		}

	}
	
}
