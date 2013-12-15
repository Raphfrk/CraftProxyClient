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

import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.raphfrk.craftproxyclient.net.types.ByteType;
import com.raphfrk.craftproxyclient.net.types.FixedSizeType;
import com.raphfrk.craftproxyclient.net.types.Type;

public class PacketRegistry {
	
	@SuppressWarnings("rawtypes")
	private final Type[][] packetInfo = new Type[256][];
	
	@SuppressWarnings("rawtypes")
	private final Type[][] compressedPacketInfo = new Type[256][];
	
	private boolean setupComplete = false;
	
	@SuppressWarnings("rawtypes")
	protected PacketRegistry register(int id, Type[] types) {
		if (setupComplete) {
			throw new IllegalStateException("New packets may not be added after register setup is complete");
		}
		if (id < 0 || id > 255) {
			throw new IllegalStateException("Packet id out of range");
		}
		if (packetInfo[id] != null) {
			throw new IllegalStateException("Packet id " + id + " already in use");
		}
		packetInfo[id] = types;
		return this;
	}
	
	protected PacketRegistry done() {
		for (int i = 0; i < 256; i++) {
			if (packetInfo[i] != null) {
				@SuppressWarnings("rawtypes")
				Type[] newArr = new Type[packetInfo[i].length + 1];
				newArr[0] = new ByteType();
				for (int j = 1; j < newArr.length; j++) {
					newArr[j] = packetInfo[i][j - 1];
				}
				packetInfo[i] = newArr;
				compressPacket(i);
			}
		}
		setupComplete = true;
		return this;
	}
	
	@SuppressWarnings("rawtypes")
	protected void compressPacket(int i) {
		int length = -1;
		Type[] types = packetInfo[i];
		ArrayList<Type<?>> compressed = new ArrayList<Type<?>>(types.length);
		int j = 0;
		for (j = 0; j < types.length; j++) {
			int fixedSize = types[j].getFixedSize();
			if (fixedSize != -1) {
				if (length == -1) {
					length = fixedSize;
				} else {
					length += fixedSize;
				}
			} else {
				if (length > 0) {
					compressed.add(new FixedSizeType(length));
					length = -1;
				}
				compressed.add(types[j]);
			}
		}
		if (length > 0) {
			compressed.add(new FixedSizeType(length));
		}
		compressedPacketInfo[i] = compressed.toArray(new Type[0]);
	}
	
	@SuppressWarnings("rawtypes")
	public Type[] getPacketInfo(int id) {
		return packetInfo[id];
	}
	
	@SuppressWarnings("rawtypes")
	public Type[] getCompressedPacketInfo(int id) {
		return compressedPacketInfo[id];
	}
	
	public Packet getPacket(int id, Object[] values, ByteBuffer buf, int offset, int length) {
		return new Packet(id, values, buf, offset, length);
	}

}
