package com.raphfrk.craftproxyclient.net.protocol.p164;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import com.raphfrk.craftproxyclient.net.protocol.ClientInfo;
import com.raphfrk.craftproxyclient.net.protocol.Handshake;
import com.raphfrk.craftproxyclient.net.protocol.PacketChannel;
import com.raphfrk.craftproxyclient.net.protocol.PacketRegistry;
import com.raphfrk.craftproxyclient.net.protocol.Protocol;

public class P164Protocol extends Protocol {
	
	public P164Protocol() {
		super(new P164PacketRegistry());
	}

	@Override
	public void handleLogin(Handshake handshake, PacketChannel client, PacketChannel server, InetSocketAddress serverAddr) throws IOException {
		P164Handshake h = (P164Handshake) handshake;
		h.setServerPort(serverAddr.getPort());
		h.setServerhost(serverAddr.getHostString());
		server.writePacket(h);
		int id = server.getPacketId();
		if (id != 0xFD) {
			throw new IOException("Unexpected packet received during login " + id);
		}
		
		/*
		if (loginInfo == null) {
			client.writePacket(new P164Kick("Please enter password into proxy"));

			

			throw new IOException("Unknown password");
		}
		P164EncryptionKeyRequest request = new P164EncryptionKeyRequest(server.getPacket());*/
		
		
		
	}
	
	@Override
	public void sendKick(String message, PacketChannel client) throws IOException {
		client.writePacket(new P164Kick(message));
	}

}
