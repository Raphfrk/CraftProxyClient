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
package com.raphfrk.craftproxyclient.net.protocol.p172Play;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONObject;

import com.raphfrk.craftproxyclient.net.protocol.Handshake;
import com.raphfrk.craftproxyclient.net.protocol.Packet;
import com.raphfrk.craftproxyclient.net.protocol.PacketChannel;
import com.raphfrk.craftproxyclient.net.protocol.Protocol;
import com.raphfrk.craftproxyclient.net.protocol.p17x.P17xHandshake;
import com.raphfrk.craftproxyclient.net.protocol.p17x.P17xKick;
import com.raphfrk.craftproxyclient.net.types.VarIntStringType;
import com.raphfrk.craftproxyclient.net.types.values.BulkData;
import com.raphfrk.craftproxycommon.compression.CompressionManager;
import com.raphfrk.craftproxycommon.message.MessageManager;
import com.raphfrk.craftproxycommon.message.SubMessage;

public class P172PlayProtocol extends Protocol {
	
	public P172PlayProtocol() {
		super(new P172PlayPacketRegistry());
	}
	
	public P17xHandshake getHandshake(Packet p) {
		return new P17xHandshake(p);
	}
	
	@Override
	public String getName() {
		return "1.72 (4)";
	}

	@Override
	public void sendKick(String message, PacketChannel client) throws IOException {
		client.writePacket(getKick(message));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Packet getKick(String message) {
		JSONObject obj = new JSONObject();
		obj.put("text", message);
		return new P17xKick(0x40, obj.toJSONString());
	}

	@Override
	public Packet convertSubMessageToPacket(SubMessage message) throws IOException {
		int channelNameLength = VarIntStringType.stringToLength(MessageManager.getChannelName());
		byte[] data = MessageManager.encode(message.getSubCommand(), message.getData());
		return new Packet(0x17, new Object[] {1 + channelNameLength + 2 + data.length, 0x17, MessageManager.getChannelName(), data});

	}

	@Override
	public void sendSubMessage(SubMessage s, PacketChannel client) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isKickMessage(int id, boolean toServer) {
		return !toServer && id == 0x40;
	}

	@Override
	public Protocol handleLogin(Handshake handshake, PacketChannel client, PacketChannel server, InetSocketAddress serverAddr) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isMessagePacket(int id, boolean toServer) {
		return !toServer && id == 0x3F;
	}

	@Override
	public String getMessageChannel(Packet p) {
		return (String) p.getField(2);
	}

	@Override
	public byte[] getMessageData(Packet p) {
		return (byte[]) p.getField(3);
	}

	@Override
	public SubMessage convertPacketToSubMessage(Packet p) throws IOException {
		String channel = (String) p.getField(2);
		if (!channel.equals(MessageManager.getChannelName())) {
			throw new IOException("Incorrect channel name " + channel);
		}
		byte[] data = (byte[]) p.getField(3);
		return MessageManager.decode(data);
	}

	@Override
	public Packet getRegisterPacket(String channel) {
		int registerLength = VarIntStringType.stringToLength("REGISTER");
		byte[] channelName = MessageManager.getChannelName().getBytes(StandardCharsets.UTF_8);
		return new Packet(0x17, new Object[] {1 + registerLength + 2 + channelName.length, 0x17, "REGISTER", channelName});
	}

	@Override
	public boolean isDataPacket(int id) {
		return id == 0x21 || id == 0x26;
	}

	@Override
	public byte[] getDataArray(Packet p) {
		if (p.getId() == 0x21) {
			int primaryBitmask = ((Short) p.getField(5)) & 0xFFFF;
			int sections = Integer.bitCount(primaryBitmask);
			int maxSize = 256 + sections * 16384;
			
			byte[] data = (byte[]) p.getField(7);
			
			byte[] inflatedData = new byte[maxSize];
			int inflatedSize = CompressionManager.inflate(data, inflatedData);
			byte[] inflatedDataResized = new byte[inflatedSize];
			System.arraycopy(inflatedData, 0, inflatedDataResized, 0, inflatedSize);
			return inflatedDataResized;
		} else  if (p.getId() == 0x26) {
			BulkData d = (BulkData) p.getField(2);
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
	public boolean setDataArray(Packet p, byte[] data) {
		if (p.getId() == 0x21) {
			byte[] deflatedData = new byte[data.length + 100];
			int size = CompressionManager.deflate(data, deflatedData);
			byte[] deflatedDataResized = new byte[size];
			System.arraycopy(deflatedData, 0, deflatedDataResized, 0, size);
			p.setField(7, deflatedDataResized);
			int length = 1 + 4 + 4 + 1 + 2 + 2 + 4 + deflatedDataResized.length;
			p.setField(0, length);
		} else if (p.getId() == 0x26) {
			byte[] deflatedData = new byte[data.length + 100];
			int size = CompressionManager.deflate(data, deflatedData);
			byte[] deflatedDataResized = new byte[size];
			System.arraycopy(deflatedData, 0, deflatedDataResized, 0, size);
			BulkData d = (BulkData) p.getField(2);
			d.setChunkData(deflatedDataResized);
			int length = 1 + 2 + 4 + 1 + deflatedDataResized.length + (4 + 4 + 2 + 2) * d.getChunks();
			p.setField(0, length);
		} else {
			return false;
		}
		return true;
	}

}
