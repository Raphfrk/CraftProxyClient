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

import com.raphfrk.craftproxyclient.net.protocol.Handshake;
import com.raphfrk.craftproxyclient.net.protocol.Packet;
import com.raphfrk.craftproxyclient.net.protocol.PacketRegistry;
import com.raphfrk.craftproxyclient.net.protocol.p164.P164Handshake;
import com.raphfrk.craftproxyclient.net.protocol.p164.P164PacketRegistry;
import com.raphfrk.craftproxyclient.net.types.String16Type;
import com.raphfrk.craftproxyclient.net.types.Type;

public class P164BootstrapPacketRegistry extends PacketRegistry {
	
	private final static Type<String> tString = new String16Type();
	
	private final static PacketRegistry[] registry = new PacketRegistry[256];
	
	static {
		registry[78] = new P164PacketRegistry();
	}
	
	public P164BootstrapPacketRegistry() {
		super.register(2, new Type[] {tByte, tString, tString, tInt});
		super.register(0xFC, new Type[] {tShortByteArray, tShortByteArray});
		super.register(0xFD, new Type[] {tString, tShortByteArray, tShortByteArray});
		super.done();
	}
	
	public P164Handshake getHandshake(Packet p) {
		return new P164Handshake(p);
	}
	
	public PacketRegistry getRegistry(Handshake handshake) {
		return registry[((P164Handshake) handshake).getProtocolVersion()];
	}

}
