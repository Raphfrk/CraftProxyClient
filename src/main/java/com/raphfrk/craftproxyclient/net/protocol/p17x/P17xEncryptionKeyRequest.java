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

import com.raphfrk.craftproxyclient.net.protocol.Packet;
import com.raphfrk.craftproxyclient.net.types.VarIntStringType;

public class P17xEncryptionKeyRequest extends Packet {
	
	public P17xEncryptionKeyRequest(Packet p) {
		super(p);
	}
	
	public P17xEncryptionKeyRequest(String serverId, byte[] pubKey, byte[] token) {
		super(0x01, new Object[] {getLength(serverId, pubKey, token), 0x01, serverId, pubKey, token});
	}
	
	public String getServerId() {
		return (String) getField(2);
	}
	
	public byte[] getPubKey() {
		return (byte[]) getField(3);
	}
	
	public byte[] getToken() {
		return (byte[]) getField(4);
	}
	
	private static int getLength(String serverId, byte[] pubKey, byte[] token) {
		return VarIntStringType.stringToLength(serverId) + 2 + pubKey.length + 2 + token.length;
	}

}
