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
package com.raphfrk.craftproxyclient.net.protocol.p164bootstrap;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.raphfrk.craftproxyclient.message.SubMessage;
import com.raphfrk.craftproxyclient.net.protocol.Handshake;
import com.raphfrk.craftproxyclient.net.protocol.Packet;
import com.raphfrk.craftproxyclient.net.protocol.PacketChannel;
import com.raphfrk.craftproxyclient.net.protocol.Protocol;
import com.raphfrk.craftproxyclient.net.protocol.p164.P164Handshake;
import com.raphfrk.craftproxyclient.net.protocol.p164.P164Protocol;

public class P164BootstrapProtocol extends Protocol {
	
	private final static Protocol[] protocol = new Protocol[256];
	
	static {
		protocol[78] = new P164Protocol();
	}

	public P164BootstrapProtocol() {
		super(new P164BootstrapPacketRegistry());
	}
	
	public P164Handshake getHandshake(Packet p) {
		return new P164Handshake(p);
	}
	
	public Protocol getProtocol(Handshake handshake) {
		return protocol[((P164Handshake) handshake).getProtocolVersion()];
	}
	
	public void handlePing(PacketChannel client) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append((char) 0xA7);
		sb.append((char) 0x31);
		sb.append((char) 0);
		sb.append("78");
		sb.append((char) 0);
		sb.append("1.64");
		sb.append((char) 0);
		sb.append("CraftProxy Client");
		sb.append((char) 0);
		sb.append("0");
		sb.append((char) 0);
		sb.append("20");
		client.writePacket(new Packet(0xFF, new Object[] {(byte) 0xFF, sb.toString()}));
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
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendKick(String message, PacketChannel client) throws IOException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isKickMessage(int id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Packet getKick(String message) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean handleLogin(Handshake handshake, PacketChannel client, PacketChannel server, InetSocketAddress serverAddr) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isMessagePacket(int id) {
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
	public void setDataArray(Packet p, byte[] data) {
		throw new UnsupportedOperationException();
	}

}
