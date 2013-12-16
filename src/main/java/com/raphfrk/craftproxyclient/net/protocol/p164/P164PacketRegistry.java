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
package com.raphfrk.craftproxyclient.net.protocol.p164;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

import org.json.simple.JSONObject;

import com.raphfrk.craftproxyclient.net.protocol.Handshake;
import com.raphfrk.craftproxyclient.net.protocol.PacketChannel;
import com.raphfrk.craftproxyclient.net.protocol.PacketRegistry;
import com.raphfrk.craftproxyclient.net.types.String16Type;
import com.raphfrk.craftproxyclient.net.types.Type;

public class P164PacketRegistry extends PacketRegistry {
	
	private final static Type<String> tString = new String16Type();
	
	public P164PacketRegistry() {
		super.register(2, new Type[] {tByte, tString, tString, tInt});
		super.register(0xFC, new Type[] {tShortByteArray, tShortByteArray});
		super.register(0xFD, new Type[] {tString, tShortByteArray, tShortByteArray});
		super.register(0xFF, new Type[] {tString});
		super.done();
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
