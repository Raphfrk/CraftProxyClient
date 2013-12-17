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
package com.raphfrk.craftproxyclient.net.protocol;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Protocol {
	
	private final PacketRegistry registry;
	
	protected Protocol(PacketRegistry registry) {
		this.registry = registry;
	}

	public PacketRegistry getPacketRegistry() {
		return registry;
	}
	
	public void sendKick(String message, PacketChannel client) throws IOException {
		throw new IllegalStateException("Base Protocol class cannot send kick");
	}
	
	public void handleLogin(Handshake handshake, PacketChannel client, PacketChannel server, InetSocketAddress serverAddr) throws IOException {
		throw new IllegalStateException("Base Protocol class cannot handle login");
	}
	
}
