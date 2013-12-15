package com.raphfrk.craftproxyclient.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import com.raphfrk.craftproxyclient.net.protocol.PacketChannel;
import com.raphfrk.craftproxyclient.net.protocol.PacketRegistry;
import com.raphfrk.craftproxyclient.net.protocol.p164.P164Handshake;
import com.raphfrk.craftproxyclient.net.protocol.p164bootstrap.P164BootstrapPacketRegistry;

public class ConnectionListener extends Thread {
	
	private final static int BUFFER_SIZE = 4 * 1024 * 1024;
	
	private final AtomicBoolean running = new AtomicBoolean();
	private final ServerSocketChannel socket;
	
	private final P164BootstrapPacketRegistry p164Bootstrap = new P164BootstrapPacketRegistry();
	
	public ConnectionListener(int port) throws IOException {
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
				PacketChannel packetChannel = new PacketChannel(c, BUFFER_SIZE);
				try {
					int id = packetChannel.getPacketId();
					if (id == 2) {
						// 1.64 protocol
						packetChannel.setRegistry(p164Bootstrap);
						P164Handshake handshake = p164Bootstrap.getHandshake(packetChannel.getPacket());
						PacketRegistry registry = p164Bootstrap.getRegistry(handshake);
						packetChannel.setRegistry(registry);
						registry.handleLogin(handshake);
						
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
