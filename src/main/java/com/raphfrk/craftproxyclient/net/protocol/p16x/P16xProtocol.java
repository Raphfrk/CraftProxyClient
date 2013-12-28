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
package com.raphfrk.craftproxyclient.net.protocol.p16x;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
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
import com.raphfrk.craftproxyclient.message.MessageManager;
import com.raphfrk.craftproxyclient.message.SubMessage;
import com.raphfrk.craftproxyclient.net.CryptByteChannelWrapper;
import com.raphfrk.craftproxyclient.net.auth.AuthManager;
import com.raphfrk.craftproxyclient.net.protocol.CompressionManager;
import com.raphfrk.craftproxyclient.net.protocol.Handshake;
import com.raphfrk.craftproxyclient.net.protocol.Packet;
import com.raphfrk.craftproxyclient.net.protocol.PacketChannel;
import com.raphfrk.craftproxyclient.net.protocol.PacketRegistry;
import com.raphfrk.craftproxyclient.net.protocol.Protocol;
import com.raphfrk.craftproxyclient.net.types.values.BulkData;

public class P16xProtocol extends Protocol {
	
	private final String name;
	
	public P16xProtocol(String name, PacketRegistry registry) {
		super(registry);
		this.name = name;
	}

	@Override
	public boolean handleLogin(Handshake handshake, PacketChannel client, PacketChannel server, InetSocketAddress serverAddr) throws IOException {
		
		P16xHandshake h = (P16xHandshake) handshake;

		String username = h.getUsername();
		if (username == null || !username.equals(AuthManager.getUsername())) {
			sendKick("Login mismatch, proxy logged as " + AuthManager.getUsername() + " client logged in as " + username, client);
			return false;
		}
		h.setServerPort(serverAddr.getPort());
		h.setServerhost(serverAddr.getHostString());
		server.writePacket(h);
		
		int id = server.getPacketId();
		if (id == 0xFF) {
			server.transferPacket(client);
			return false;
		} else if (id != 0xFD) {
			sendKick("Expecting Encrypt Key Request packet", client);
			return false;
		}
		
		P16xEncryptionKeyRequest request = new P16xEncryptionKeyRequest(server.getPacket());
		
		byte[] secret = Crypt.getBytes(16);
		
		if (!authSession(secret, client, request)) {
			return false;
		}
		
		if (!sendEncryptionKeyResponse(secret, client, server, request)) {
			return false;
		}
		
		id = server.getPacketId();
		if (id == 0xFF) {
			server.transferPacket(client);
			return false;
		} if (id != 0xFC) {
			sendKick("Expecting Encrypt Key Response packet", client);
			return false;
		}
		
		P16xEncryptionKeyResponse response = new P16xEncryptionKeyResponse(server.getPacket());
		if (response.getPubKey().length != 0 || response.getToken().length != 0) {
			sendKick("Invalid Encrypt Key Response packet", client);
			return false;
		}
		
		enableEncryption(server, client, secret);
		
		P16xClientStatus status = new P16xClientStatus((byte) 0);
		server.writePacket(status);
		
		id = server.getPacketId();
		if (id == 0xFF) {
			server.transferPacket(client);
			return false;
		} if (id != 0x01) {
			sendKick("Didn't receive login packet, received packet " + id, client);
			return false;
		}
		
		P16xLoginRequest login = new P16xLoginRequest(server.getPacket());
		client.writePacket(login);
		
		return true;
	}
	
	@Override
	public void sendKick(String message, PacketChannel client) throws IOException {
		client.writePacket(getKick(message));
	}
	
	@Override
	public boolean isKickMessage(int id) {
		return id == 0xFF;
	}
	
	@Override
	public Packet getKick(String message) {
		return new P16xKick(message);
	}
	
	@Override
	public void sendSubMessage(SubMessage message, PacketChannel client) throws IOException {
		client.writePacket(convertSubMessageToPacket(message));
	}
	
	@Override
	public Packet convertSubMessageToPacket(SubMessage message) throws IOException {
		return new Packet(0xFA, new Object[] {(byte) 0xFA, MessageManager.getChannelName(), MessageManager.encode(message.getSubCommand(), message.getData())});
	}
	
	@Override
	public String getName() {
		return name;
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
	
	private boolean authSession(byte[] secret, PacketChannel client, P16xEncryptionKeyRequest request) throws IOException {
		String hash = SHA1Hash(new Object[] {request.getServerId(), secret, request.getPubKey()});
		
		AuthManager.authServer(hash);
		
		return true;
	}
	
	private boolean sendEncryptionKeyResponse(byte[] secret, PacketChannel client, PacketChannel server, P16xEncryptionKeyRequest request) throws IOException {
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
		
		server.writePacket(new P16xEncryptionKeyResponse(encryptedSecret, encryptedToken));
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

	@Override
	public boolean isMessagePacket(int id) {
		return id == 0xFA;
	}
	
	@Override
	public String getMessageChannel(Packet p) {
		return (String) p.getField(1);
	}
	
	@Override
	public byte[] getMessageData(Packet p) {
		return (byte[]) p.getField(2);
	}

	@Override
	public SubMessage convertPacketToSubMessage(Packet p) throws IOException {
		String channel = (String) p.getField(1);
		if (!channel.equals(MessageManager.getChannelName())) {
			throw new IOException("Incorrect channel name " + channel);
		}
		byte[] data = (byte[]) p.getField(2);
		return MessageManager.decode(data);
	}

	@Override
	public Packet getRegisterPacket(String channel) {
		return new Packet(0xFA, new Object[] {(byte) 0xFA, "REGISTER", MessageManager.getChannelName().getBytes(StandardCharsets.UTF_8)});
	}

	@Override
	public boolean isDataPacket(int id) {
		return id == 0x33 || id == 0x38;
	}

	@Override
	public byte[] getDataArray(Packet p) {
		if (p.getId() == 0x33) {
			int primaryBitmask = ((Short) p.getField(4)) & 0xFFFF;
			int sections = Integer.bitCount(primaryBitmask);
			int maxSize = 256 + sections * 16384;
			
			byte[] data = (byte[]) p.getField(6);
			
			byte[] inflatedData = new byte[maxSize];
			int inflatedSize = CompressionManager.inflate(data, inflatedData);
			byte[] inflatedDataResized = new byte[inflatedSize];
			System.arraycopy(inflatedData, 0, inflatedDataResized, 0, inflatedSize);
			return inflatedDataResized;
		} else  if (p.getId() == 0x38) {
			BulkData d = (BulkData) p.getField(1);
			int chunks = d.getChunks();
			int maxSize = chunks * 16384 * 16;
			byte[] inflatedData = new byte[maxSize];
			int inflatedSize = CompressionManager.inflate(d.getChunkData(), inflatedData);
			byte[] inflatedDataResized = new byte[inflatedSize];
			System.arraycopy(inflatedData, 0, inflatedDataResized, 0, inflatedSize);
			return inflatedDataResized;
		} else {
			return null;
		}
	}

	@Override
	public void setDataArray(Packet p, byte[] data) {
		if (p.getId() == 0x33) {
			byte[] deflatedData = new byte[data.length + 100];
			int size = CompressionManager.deflate(data, deflatedData);
			byte[] deflatedDataResized = new byte[size];
			System.arraycopy(deflatedData, 0, deflatedDataResized, 0, size);
			p.setField(6, deflatedDataResized);
		} else if (p.getId() == 0x38) {
			byte[] deflatedData = new byte[data.length + 100];
			int size = CompressionManager.deflate(data, deflatedData);
			byte[] deflatedDataResized = new byte[size];
			System.arraycopy(deflatedData, 0, deflatedDataResized, 0, size);
			BulkData d = (BulkData) p.getField(1);
			d.setChunkData(deflatedDataResized);
		}
	}

}
