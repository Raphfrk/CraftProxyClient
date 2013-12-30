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
package com.raphfrk.craftproxyclient.net.protocol.p17xlogin;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.MessageDigest;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.modes.CFBBlockCipher;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.json.simple.JSONObject;

import com.raphfrk.craftproxyclient.crypt.Crypt;
import com.raphfrk.craftproxyclient.net.CryptByteChannelWrapper;
import com.raphfrk.craftproxyclient.net.SingleByteByteChannelWrapper;
import com.raphfrk.craftproxyclient.net.auth.AuthManager;
import com.raphfrk.craftproxyclient.net.protocol.Handshake;
import com.raphfrk.craftproxyclient.net.protocol.Packet;
import com.raphfrk.craftproxyclient.net.protocol.PacketChannel;
import com.raphfrk.craftproxyclient.net.protocol.PacketRegistry;
import com.raphfrk.craftproxyclient.net.protocol.Protocol;
import com.raphfrk.craftproxyclient.net.protocol.p172Play.P172PlayProtocol;
import com.raphfrk.craftproxyclient.net.protocol.p17x.P17xEncryptionKeyRequest;
import com.raphfrk.craftproxyclient.net.protocol.p17x.P17xEncryptionKeyResponse;
import com.raphfrk.craftproxyclient.net.protocol.p17x.P17xHandshake;
import com.raphfrk.craftproxyclient.net.protocol.p17x.P17xKick;
import com.raphfrk.craftproxyclient.net.protocol.p17x.P17xLoginStart;
import com.raphfrk.craftproxyclient.net.protocol.p17x.P17xLoginSuccess;
import com.raphfrk.craftproxyclient.net.protocol.p17x.P17xProtocol;
import com.raphfrk.craftproxyclient.net.protocol.p17xhandshake.P17xHandshakePacketRegistry;

public class P17xLoginProtocol extends P17xProtocol {
	
	private static PacketRegistry handshakePacketRegistry = new P17xHandshakePacketRegistry();
	private static P172PlayProtocol p172Protocol = new P172PlayProtocol();
	
	public P17xLoginProtocol() {
		super(new P17xLoginPacketRegistry());
	}
	
	@Override
	public Protocol handleLogin(Handshake handshake, PacketChannel client, PacketChannel server, InetSocketAddress serverAddr) throws IOException {
		P17xHandshake h = (P17xHandshake) handshake;

		if (h.getNextState() != 2) {
			sendKick("Unknown handshake next state " + h.getNextState(), client);
			return null;
		}
		
		h.setServerPort(serverAddr.getPort());
		h.setServerhost(serverAddr.getHostString());
		
		server.setRegistry(handshakePacketRegistry);
		server.writePacket(h);
		server.setRegistry(getPacketRegistry());
		
		int id = client.getPacketId();

		if (this.isKickMessage(id, true)) {
			client.transferPacket(server);
			sendKick("Kick message received from client", client);
			return null;
		} else if (id != 0) {
			sendKick("Expected LoginStart message", client);
			return null;
		}
		
		P17xLoginStart loginStart = new P17xLoginStart(client.getPacket());
		
		if (loginStart.getUsername() == null || !loginStart.getUsername().equals(AuthManager.getUsername())) {
			sendKick("Login mismatch, proxy logged as " + AuthManager.getUsername() + " client logged in as " + loginStart.getUsername(), client);
			return null;
		}
		
		server.writePacket(loginStart);
		
		id = server.getPacketId();
		if (this.isKickMessage(id, false)) {
			server.transferPacket(client);
			return null;
		} else if (id != 0x01) {
			sendKick("Expecting Encrypt Key Request packet", client);
			return null;
		}
		
		P17xEncryptionKeyRequest request = new P17xEncryptionKeyRequest(server.getPacket());
		
		byte[] secret = Crypt.getBytes(16);
		
		if (!authSession(secret, client, request)) {
			return null;
		}
		
		if (!sendEncryptionKeyResponse(secret, client, server, request)) {
			return null;
		}
		
		enableEncryption(server, client, secret);
		
		id = server.getPacketId();
		if (this.isKickMessage(id, false)) {
			server.transferPacket(client);
			return null;
		} if (id != 0x02) {
			System.out.println("Id is " + id);
			sendKick("Expecting Login Success packet", client);
			return null;
		}
		
		P17xLoginSuccess success = new P17xLoginSuccess(server.getPacket());
		client.writePacket(success);

		return p172Protocol;
	}
	
	@Override
	public boolean isKickMessage(int id, boolean toServer) {
		return !toServer && id == 0x00;
	}
	
	private boolean authSession(byte[] secret, PacketChannel client, P17xEncryptionKeyRequest request) throws IOException {
		String hash = SHA1Hash(new Object[] {request.getServerId(), secret, request.getPubKey()});

		AuthManager.authServer17(hash);
		
		return true;
	}
	
	private boolean sendEncryptionKeyResponse(byte[] secret, PacketChannel client, PacketChannel server, P17xEncryptionKeyRequest request) throws IOException {
		AsymmetricBlockCipher rsa = new PKCS1Encoding(new RSAEngine());
		
		AsymmetricKeyParameter publicKey = PublicKeyFactory.createKey(request.getPubKey());
		
		rsa.init(true, publicKey);
		
		byte[] encryptedSecret;
		byte[] encryptedToken;
		
		try {
			encryptedSecret = rsa.processBlock(secret, 0, secret.length);
		} catch (InvalidCipherTextException e) {
			sendKick("Unable to encrypt shared secret " + e.getMessage(), client);
			return false;
		}
		
		try {
			encryptedToken = rsa.processBlock(request.getToken(), 0, request.getToken().length);
		} catch (InvalidCipherTextException e) {
			sendKick("Unable to encrypt token " + e.getMessage(), client);
			return false;
		}
		
		server.writePacket(new P17xEncryptionKeyResponse(encryptedSecret, encryptedToken));
		return true;
	}
	
	private void enableEncryption(PacketChannel server, PacketChannel client, byte[] secret) {
		BufferedBlockCipher out = new BufferedBlockCipher(new CFBBlockCipher(new AESEngine(), 8));
		BufferedBlockCipher in = new BufferedBlockCipher(new CFBBlockCipher(new AESEngine(), 8));
		CipherParameters params = new ParametersWithIV(new KeyParameter(secret), secret);
		out.init(true, params);
		in.init(false, params);
		
		// Unencrypted
		client.setWrappedChannel(client.getRawChannel());

		// AES
		server.setWrappedChannel(new CryptByteChannelWrapper(server.getRawChannel(), out, in));
	}
	
	@Override
	public void sendKick(String message, PacketChannel client) throws IOException {
		client.writePacket(getKick(message));
	}
	
	@Override
	public Packet getKick(String message) {
		JSONObject obj = new JSONObject();
		obj.put("text", message);
		return new P17xKick(0, obj.toJSONString());
	}
	
	private static String SHA1Hash(Object[] input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.reset();

			for (Object o : input) {
				if (o instanceof String) {
					md.update(((String) o).getBytes("ISO_8859_1"));
				} else if (o instanceof byte[]) {
					md.update((byte[]) o);
				} else {
					return null;
				}
			}

			byte[] digest = md.digest();

			BigInteger bigInt = new BigInteger(digest);
			
			return bigInt.toString(16);
		} catch (Exception ioe) {
			return null;
		}
	}
	
    public static String toHex(byte[] arr) {
        String s = "";
        for (byte b : arr) {
            s = s + Integer.toHexString(b & 0xFF) + ", ";
        }
        return s;
    }


}
