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

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

import com.raphfrk.craftproxyclient.gui.CraftProxyGUI;
import com.raphfrk.craftproxyclient.net.protocol.Handshake;
import com.raphfrk.craftproxyclient.net.protocol.Packet;
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
	private final AtomicInteger serverDataIn = new AtomicInteger();
	private final AtomicInteger serverDataOut = new AtomicInteger();
	private final AtomicInteger clientDataIn = new AtomicInteger();
	private final AtomicInteger clientDataOut = new AtomicInteger();

	private final P164BootstrapProtocol p164Bootstrap = new P164BootstrapProtocol();

	public ConnectionListener(CraftProxyGUI gui, int port, String serverHostname, int serverPort) throws IOException {
		super("ConnectionListener");
		this.gui = gui;
		this.serverAddr = new InetSocketAddress(serverHostname, serverPort);
		this.localAddr = new InetSocketAddress(port);
		this.socket = ServerSocketChannel.open();
		this.socket.socket().bind(localAddr);
	}

	public void run() {
		try {
			while (!isInterrupted()) {
				TransferConnection serverToClient = null;
				TransferConnection clientToServer = null;
				String port = localAddr.getPort() != 25565 ? (":" + localAddr.getPort()) : "";
				gui.setStatus("Waiting for connection, connect to localhost" + port);
				SocketChannel c;
				try {
					c = socket.accept();
				} catch (IOException e) {
					continue;
				}
				serverDataIn.set(0);
				serverDataOut.set(0);
				clientDataIn.set(0);
				clientDataOut.set(0);
				Protocol protocol = null;
				PacketChannel client = null;
				try {
					client = new PacketChannel(c, clientDataIn, clientDataOut, BUFFER_SIZE, WRITE_BUFFER_SIZE);
					try {
						int id = client.getPacketId();
						
						Handshake handshake = null;

						if (id == 0xFE) {
							client.setRegistry(p164Bootstrap.getPacketRegistry());
							Packet ping = client.getPacket();
							if ((Byte) ping.getField(1) != 1) {
								System.out.println("Ping code " + ping.getField(1));
								continue;
							}
							gui.setStatus("Ping received");
							SocketChannel s = SocketChannel.open();
							try {
								s.connect(serverAddr);
							} catch (IOException e) {
								continue;
							}
							PacketChannel server = new PacketChannel(s, serverDataIn, serverDataOut, BUFFER_SIZE, WRITE_BUFFER_SIZE);
							server.setRegistry(p164Bootstrap.getPacketRegistry());
							server.writePacket(ping);
							Packet plugin = client.getPacket();
							server.writePacket(plugin);
							server.writePacket(plugin);
							Packet kick = server.getPacket();
							client.writePacket(kick);
							continue;
						}

						if (id == 2) {
							// 1.64 protocol
							client.setRegistry(p164Bootstrap.getPacketRegistry());
							handshake = p164Bootstrap.getHandshake(client.getPacket());
							protocol = p164Bootstrap.getProtocol(handshake);
							if (protocol == null) {
								gui.setStatus(p164Bootstrap.getProtocolFailInfo(handshake));
								continue;
							}
							client.setRegistry(protocol.getPacketRegistry());
						}


						if (protocol == null) {
							continue;
						}

						gui.setStatus("Connection received, login started", "Protocol: " + protocol.getName());

						SocketChannel s = SocketChannel.open();
						try {
							s.connect(serverAddr);
						} catch (IOException e) {
							protocol.sendKick("Unable to connect to server", client);
							continue;
						}

						PacketChannel server = null;
						try {
							server = new PacketChannel(s, serverDataIn, serverDataOut, BUFFER_SIZE, WRITE_BUFFER_SIZE);
							server.setRegistry(protocol.getPacketRegistry());
							protocol.handleLogin(handshake, client, server, serverAddr);
						} catch (IOException e) {
							if (server != null) {
								protocol.sendKick("Connection closing, " + e.getMessage(), client);
								s.close();
								continue;
							}
						}

						gui.setStatus("Login completed", "Protocol: " + protocol.getName());

						long capacity = gui.getCapacity();

						ConnectionManager manager = null;
						
						try {
							manager = new ConnectionManager(new File("cache"), capacity, gui);
							try {
								serverToClient = new TransferConnection("Server to client", protocol, server, client, manager, this);
								clientToServer = new TransferConnection("Client to server", protocol, client, server, null, this);
							} catch (RuntimeException ee) {
								ee.printStackTrace();
								throw ee;
							}
							serverToClient.setOther(clientToServer);
							clientToServer.setOther(serverToClient);

							serverToClient.start();
							clientToServer.start();

							serverToClient.join();
							clientToServer.join();
						} catch (InterruptedException e) {
							break;
						} finally {
							if (manager != null) {
								manager.shutdown();
							}
							if (serverToClient != null) {
								serverToClient.queuePacket(protocol.getKick("Proxy halted"));
							} else {
								client.writePacket(protocol.getKick("Unable to start packet transfer threads"));
							}
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
			}
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
			}
			gui.setDone();
		}
	}
	
	public void updateGUIBandwidth() {
		int server = serverDataIn.get() + serverDataOut.get();
		int client = clientDataIn.get() + clientDataOut.get();
		if (client == 0) {
			return;
		}
		int comp = 100 - ((100 * server) / client);
		String text = "Bandwidth down " + (serverDataIn.get() / 1024) + "kB, up " + (serverDataOut.get() / 1024) + "kB (" + comp + "% compression)";
		gui.setStatusReplace("Bandwidth", text);
	}
	
	public void interrupt(String message) {
		gui.setStatus("IO error, " + message);
		super.interrupt();
	}
}
