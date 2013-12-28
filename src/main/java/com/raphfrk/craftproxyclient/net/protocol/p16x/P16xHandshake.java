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

import com.raphfrk.craftproxyclient.net.protocol.Handshake;
import com.raphfrk.craftproxyclient.net.protocol.Packet;

public class P16xHandshake extends Packet implements Handshake {

	public P16xHandshake(Packet p) {
		super(p);
	}
	
	public int getProtocolVersion() {
		return ((Byte) getField(1)) & 0xFF;
	}
	
	public String getUsername() {
		return (String) getField(2);
	}
	
	public String getServerhost() {
		return (String) getField(3);
	}
	
	public void setServerhost(String hostname) {
		setField(3, hostname);
	}
	
	public int getServerPort() {
		return (Integer) getField(4);
	}
	
	public void setServerPort(int port) {
		setField(4, (Integer) port);
	}

}
