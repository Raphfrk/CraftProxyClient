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

import com.raphfrk.craftproxyclient.net.types.BulkDataType;
import com.raphfrk.craftproxyclient.net.types.ByteType;
import com.raphfrk.craftproxyclient.net.types.CountSizedArrayType;
import com.raphfrk.craftproxyclient.net.types.FixedSizeType;
import com.raphfrk.craftproxyclient.net.types.IntType;
import com.raphfrk.craftproxyclient.net.types.LongType;
import com.raphfrk.craftproxyclient.net.types.MetaType;
import com.raphfrk.craftproxyclient.net.types.PropertyArrayType;
import com.raphfrk.craftproxyclient.net.types.ShortType;
import com.raphfrk.craftproxyclient.net.types.SlotArrayType;
import com.raphfrk.craftproxyclient.net.types.SlotType;
import com.raphfrk.craftproxyclient.net.types.SpawnDataType;
import com.raphfrk.craftproxyclient.net.types.TeamDataType;
import com.raphfrk.craftproxyclient.net.types.Type;
import com.raphfrk.craftproxyclient.net.types.values.Slot;

public class PacketRegistry {
	
	protected ByteType tByte = new ByteType();
	protected IntType tInt = new IntType();
	protected ShortType tShort = new ShortType();
	protected LongType tLong = new LongType();
	
	// Placeholders - packets with these types don't need to be parsed
	protected Type<Byte> tBoolean = new ByteType();
	protected Type<Integer> tFloat = new IntType();
	protected Type<Long> tDouble = new LongType();
	
	protected Type<byte[]> tByteByteArray = new CountSizedArrayType(tByte, 1);
	protected Type<byte[]> tShortByteArray = new CountSizedArrayType(tShort, 1);
	protected Type<byte[]> tIntByteArray = new CountSizedArrayType(tInt, 1);
	
	protected Type<byte[]> tByteIntArray = new CountSizedArrayType(tByte, 4);
	
	protected Type<byte[]> tIntTripleByteArray = new CountSizedArrayType(tInt, 3);
	
	protected final static BulkDataType tBulk = new BulkDataType();
	
	protected Type<Slot> tSlot = new SlotType();
	protected SlotArrayType tSlotArray = new SlotArrayType();
	protected Type<Object[][]> tMeta = new MetaType();
	protected Type<int[]> tSpawnData = new SpawnDataType();
	protected TeamDataType tTeam = new TeamDataType();
	
	protected Type<Object> tPropertyArray = new PropertyArrayType();
	
	@SuppressWarnings("rawtypes")
	private final Type[][] packetInfo = new Type[256][];
	
	@SuppressWarnings("rawtypes")
	private final Type[][] compressedPacketInfo = new Type[256][];
	
	private boolean setupComplete = false;
	
	@SuppressWarnings("rawtypes")
	protected PacketRegistry register(int id, Type ... types) {
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
	
	public int getPacketId(ByteBuffer buf) {
		if (buf.remaining() == 0) {
			return -1;
		}
		return  buf.get(buf.position()) & 0xFF;
	}

}
