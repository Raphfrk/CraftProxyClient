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

import com.raphfrk.craftproxyclient.net.protocol.p17x.P17xPacketRegistry;
import com.raphfrk.craftproxyclient.net.types.Type;
import com.raphfrk.craftproxyclient.net.types.VarIntStringType;
import com.raphfrk.craftproxyclient.net.types.VarIntType;

public class P17xHandshakePacketRegistry extends P17xPacketRegistry {
	
	private final static Type<String> tString = new VarIntStringType();
	private final static VarIntType tVarInt = new VarIntType();
	
	public P17xHandshakePacketRegistry() {
		super.register(0, tVarInt, tVarInt, tVarInt, tString, tShort, tVarInt);
		super.done();
	}
	
}
