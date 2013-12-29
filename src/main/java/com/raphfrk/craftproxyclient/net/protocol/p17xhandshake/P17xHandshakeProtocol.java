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
package com.raphfrk.craftproxyclient.net.protocol.p17xhandshake;

import com.raphfrk.craftproxyclient.net.protocol.Handshake;
import com.raphfrk.craftproxyclient.net.protocol.Packet;
import com.raphfrk.craftproxyclient.net.protocol.Protocol;
import com.raphfrk.craftproxyclient.net.protocol.p172Play.P172PlayProtocol;
import com.raphfrk.craftproxyclient.net.protocol.p17x.P17xHandshake;
import com.raphfrk.craftproxyclient.net.protocol.p17x.P17xProtocol;

public class P17xHandshakeProtocol extends P17xProtocol {
	
	private final static Protocol[] protocol = new Protocol[256];
	
	static {
		protocol[4] = new P172PlayProtocol();
	}

	public P17xHandshakeProtocol() {
		super(new P17xHandshakePacketRegistry());
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

}
