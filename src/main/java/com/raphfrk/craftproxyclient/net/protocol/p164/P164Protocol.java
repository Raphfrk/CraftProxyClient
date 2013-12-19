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

import com.raphfrk.craftproxyclient.crypt.Crypt;
import com.raphfrk.craftproxyclient.net.CryptByteChannelWrapper;
import com.raphfrk.craftproxyclient.net.auth.AuthManager;
import com.raphfrk.craftproxyclient.net.protocol.Handshake;
import com.raphfrk.craftproxyclient.net.protocol.Packet;
import com.raphfrk.craftproxyclient.net.protocol.PacketChannel;
import com.raphfrk.craftproxyclient.net.protocol.Protocol;

public class P164Protocol extends Protocol {
	
	public P164Protocol() {
		super(new P164PacketRegistry());
	}

	@Override
	public boolean handleLogin(Handshake handshake, PacketChannel client, PacketChannel server, InetSocketAddress serverAddr) throws IOException {
		
		P164Handshake h = (P164Handshake) handshake;

		String username = h.getUsername();
		if (username == null || !username.equals(AuthManager.getUsername())) {
			sendKick("Login mismatch, proxy logged as " + AuthManager.getUsername() + " client logged in as " + username, client);
			return false;
		}
		h.setServerPort(serverAddr.getPort());
		h.setServerhost(serverAddr.getHostString());
		server.writePacket(h);
		
		int id = server.getPacketId();
		if (id != 0xFD) {
			sendKick("Expecting Encrypt Key Request packet", client);
			return false;
		}
		
		P164EncryptionKeyRequest request = new P164EncryptionKeyRequest(server.getPacket());
		
		byte[] secret = Crypt.getBytes(16);
		
		if (!authSession(secret, client, request)) {
			return false;
		}
		
		if (!sendEncryptionKeyResponse(secret, client, server, request)) {
			return false;
		}
		
		id = server.getPacketId();
		if (id != 0xFC) {
			sendKick("Expecting Encrypt Key Response packet", client);
			return false;
		}
		
		P164EncryptionKeyResponse response = new P164EncryptionKeyResponse(server.getPacket());
		if (response.getPubKey().length != 0 || response.getToken().length != 0) {
			sendKick("Invalid Encrypt Key Response packet", client);
			return false;
		}
		
		enableEncryption(server, client, secret);
		
		P164ClientStatus status = new P164ClientStatus((byte) 0);
		server.writePacket(status);
		
		id = server.getPacketId();
		if (id != 0x01) {
			sendKick("Didn't receive login packet", client);
			return false;
		}
		
		P164LoginRequest login = new P164LoginRequest(server.getPacket());
		client.writePacket(login);	
		
		return true;
	}
	
	@Override
	public void sendKick(String message, PacketChannel client) throws IOException {
		client.writePacket(getKick(message));
	}
	
	@Override
	public Packet getKick(String message) {
		return new P164Kick(message);
	}
	
	@Override
	public String getName() {
		return "1.64 (78)";
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
	
	private boolean authSession(byte[] secret, PacketChannel client, P164EncryptionKeyRequest request) throws IOException {
		String hash = SHA1Hash(new Object[] {request.getServerId(), secret, request.getPubKey()});
		
		if (!AuthManager.authServer(hash)) {
			sendKick("Unable to connect to auth server", client);
			return false;
		}
		
		return true;
	}
	
	private boolean sendEncryptionKeyResponse(byte[] secret, PacketChannel client, PacketChannel server, P164EncryptionKeyRequest request) throws IOException {
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
		
		server.writePacket(new P164EncryptionKeyResponse(encryptedSecret, encryptedToken));
		return true;
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

                BigInteger bigInt = new BigInteger(md.digest());

                if (bigInt.compareTo(BigInteger.ZERO) < 0) {
                        bigInt = bigInt.negate();
                        return "-" + bigInt.toString(16);
                } else {
                        return bigInt.toString(16);
                }
        } catch (Exception ioe) {
                return null;
        }
}

}
