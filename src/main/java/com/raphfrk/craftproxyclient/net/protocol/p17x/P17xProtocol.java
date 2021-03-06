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
package com.raphfrk.craftproxyclient.net.protocol.p17x;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.raphfrk.craftproxyclient.net.protocol.Handshake;
import com.raphfrk.craftproxyclient.net.protocol.Packet;
import com.raphfrk.craftproxyclient.net.protocol.PacketChannel;
import com.raphfrk.craftproxyclient.net.protocol.Protocol;
import com.raphfrk.craftproxyclient.net.protocol.p172Play.P172PlayProtocol;
import com.raphfrk.craftproxycommon.message.SubMessage;

public class P17xProtocol extends Protocol {
	
	private final static Protocol[] protocol = new Protocol[256];
	
	static {
		protocol[4] = new P172PlayProtocol();
	}

	public P17xProtocol(P17xPacketRegistry registry) {
		super(registry);
	}
	
	public P17xHandshake getHandshake(Packet p) {
		return new P17xHandshake(p);
	}
	
	public Protocol getProtocol(Handshake handshake) {
		return protocol[((P17xHandshake) handshake).getProtocolVersion()];
	}
	
	public String getProtocolFailInfo(Handshake handshake) {
		return "Unknown protocol version " + ((P17xHandshake) handshake).getProtocolVersion();
	}
	
	@Override
	public Packet convertSubMessageToPacket(SubMessage s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendSubMessage(SubMessage s, PacketChannel client) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName() {
		return "1.7.x";
	}

	@Override
	public void sendKick(String message, PacketChannel client) throws IOException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isKickMessage(int id, boolean toServer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Packet getKick(String message) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Protocol handleLogin(Handshake handshake, PacketChannel client, PacketChannel server, InetSocketAddress serverAddr) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isMessagePacket(int id, boolean toServer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SubMessage convertPacketToSubMessage(Packet p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Packet getRegisterPacket(String channel) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getMessageChannel(Packet p) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public byte[] getMessageData(Packet p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isDataPacket(int id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] getDataArray(Packet p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean setDataArray(Packet p, byte[] data) {
		throw new UnsupportedOperationException();
	}

}
