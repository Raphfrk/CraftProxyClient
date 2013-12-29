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

import java.nio.ByteBuffer;

import com.raphfrk.craftproxyclient.net.protocol.PacketRegistry;
import com.raphfrk.craftproxyclient.net.types.VarIntType;

public abstract class P17xPacketRegistry extends PacketRegistry {
	
	public P17xPacketRegistry() {
	}
	
	public int getPacketId(ByteBuffer buf) {
		int pos = buf.position();
		try {
			int length = VarIntType.getLengthRaw(buf);
			if (length == -1 || buf.remaining() < length) {
				return -1;
			}
			buf.position(length + pos);
			int idLength = VarIntType.getLengthRaw(buf);
			if (idLength == -1 || buf.remaining() < idLength) {
				return -1;
			}
			buf.position(length + pos);
			int id = VarIntType.getRaw(buf);
			return id;
		} finally {
			buf.position(pos);
		}
	}
	
}
