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

import com.raphfrk.craftproxyclient.net.protocol.p17x.P17xPacketRegistry;
import com.raphfrk.craftproxyclient.net.types.FullPacket17xType;
import com.raphfrk.craftproxyclient.net.types.Type;
import com.raphfrk.craftproxyclient.net.types.VarIntStringType;
import com.raphfrk.craftproxyclient.net.types.VarIntType;

public class P172PlayPacketRegistry extends P17xPacketRegistry {
	
	private final static Type<String> tString = new VarIntStringType();
	private final static VarIntType tVarInt = new VarIntType();
	private final static Type<byte[]> tFullPacket = new FullPacket17xType();
	
	public P172PlayPacketRegistry() {
		super.setToClient();
		for (int i = 0; i < 65; i++) {
			if (i == 0x40) {
				register(i, tVarInt, tVarInt, tString);
			} else if (i == 0x3F ){
				register(i, tVarInt, tVarInt, tString, tShortByteArray);
			} else if (i == 0x21) {
				register(i, tVarInt, tVarInt, tInt, tInt, tBoolean, tShort, tShort, tIntByteArray);
			} else if (i == 0x26) {
				register(i, tVarInt, tVarInt, tBulk);
			} else {
				register(i, tFullPacket);
			}
		}
		super.setToServer();
		for (int i = 0; i < 24; i++) {
			if (i == 0x17) {
				register(i, tVarInt, tVarInt, tString, tShortByteArray);
			} else {
				register(i, tFullPacket);
			}
		}
		super.done();
	}

}
