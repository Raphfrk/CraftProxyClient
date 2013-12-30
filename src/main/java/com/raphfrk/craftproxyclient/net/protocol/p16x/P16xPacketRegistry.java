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

import com.raphfrk.craftproxyclient.net.protocol.PacketRegistry;
import com.raphfrk.craftproxyclient.net.types.String16Type;
import com.raphfrk.craftproxyclient.net.types.Type;

public class P16xPacketRegistry extends PacketRegistry {
	
	private final static Type<String> tString = new String16Type();
	
	public P16xPacketRegistry() {
		
		addPacketIdByte();
		register(0x00, tInt);
		register(0x01, tInt, tString, tByte, tByte, tByte, tByte, tByte);
		register(0x02, tByte, tString, tString, tInt);
		register(0x03, tString);
		register(0x04, tLong, tLong);
		register(0x05, tInt, tShort, tSlot);
		register(0x06, tInt, tInt, tInt);
		register(0x07, tInt, tInt, tBoolean);
		register(0x08, tFloat, tShort, tFloat);
		register(0x09, tInt, tByte, tByte, tShort, tString);
		register(0x0A, tBoolean);
		register(0x0B, tDouble, tDouble, tDouble, tDouble, tBoolean);
		register(0x0C, tFloat, tFloat, tBoolean);
		register(0x0D, tDouble, tDouble, tDouble, tDouble, tFloat, tFloat, tBoolean);
		register(0x0E, tByte, tInt, tByte, tInt, tByte);
		register(0x0F, tInt, tByte, tInt, tByte, tSlot, tByte, tByte, tByte);
		register(0x10, tShort);
		register(0x11, tInt, tByte, tInt, tByte, tInt);
		register(0x12, tInt, tByte);
		register(0x13, tInt, tByte, tInt);
		register(0x14, tInt, tString, tInt, tInt, tInt, tByte, tByte, tShort, tMeta);
		register(0x16, tInt, tInt);
		register(0x17, tInt, tByte, tInt, tInt, tInt, tByte, tByte, tSpawnData);
		register(0x18, tInt, tByte, tInt, tInt, tInt, tByte, tByte, tByte, tShort, tShort, tShort, tMeta);
		register(0x19, tInt, tString, tInt, tInt, tInt, tInt);
		register(0x1A, tInt, tInt, tInt, tInt, tShort);
		register(0x1B, tFloat, tFloat, tBoolean, tBoolean);
		register(0x1C, tInt, tShort, tShort, tShort);
		register(0x1D, tByteIntArray);
		register(0x1E, tInt);
		register(0x1F, tInt, tByte, tByte, tByte);
		register(0x20, tInt, tByte, tByte);
		register(0x21, tInt, tByte, tByte, tByte, tByte, tByte);
		register(0x22, tInt, tInt, tInt, tInt, tByte, tByte);
		register(0x23, tInt, tByte);
		register(0x26, tInt, tByte);
		register(0x27, tInt, tInt, tByte);
		register(0x28, tInt, tMeta);
		register(0x29, tInt, tByte, tByte, tShort);
		register(0x2A, tInt, tByte);
		register(0x2B, tFloat, tShort, tShort);
		register(0x2C, tInt, tPropertyArray);
		register(0x33, tInt, tInt, tBoolean, tShort, tShort, tIntByteArray);
		register(0x34, tInt, tInt, tShort, tIntByteArray);
		register(0x35, tInt, tByte, tInt, tShort, tByte);
		register(0x36, tInt, tShort, tInt, tByte, tByte, tShort);
		register(0x37, tInt, tInt, tInt, tInt, tByte);
		register(0x38, tBulk);
		register(0x3C, tDouble, tDouble, tDouble, tFloat, tIntTripleByteArray, tFloat, tFloat, tFloat);
		register(0x3D, tInt, tInt, tByte, tInt, tInt, tBoolean);
		register(0x3E, tString, tInt, tInt, tInt, tFloat, tByte);
		register(0x3F, tString, tFloat, tFloat, tFloat, tFloat, tFloat, tFloat, tFloat, tInt);
		register(0x46, tByte, tByte);
		register(0x47, tInt, tByte, tInt, tInt, tInt);
		register(0x64, tByte, tByte, tString, tByte, tBoolean, tInt);
		register(0x65, tByte);
		register(0x66, tByte, tShort, tByte, tShort, tByte, tSlot);
		register(0x67, tByte, tShort, tSlot);
		register(0x68, tByte, tSlotArray);
		register(0x69, tByte, tShort, tShort);
		register(0x6A, tByte, tShort, tBoolean);
		register(0x6B, tShort, tSlot);
		register(0x6C, tByte, tByte);
		register(0x82, tInt, tShort, tInt, tString, tString, tString, tString);
		register(0x83, tShort, tString, tShortByteArray);
		register(0x84, tInt, tShort, tInt, tByte, tShortByteArray);
		register(0x85, tByte, tInt, tInt, tInt);
		register(0xC8, tInt, tInt);
		register(0xC9, tString, tBoolean, tShort);
		register(0xCA, tByte, tFloat, tFloat);
		register(0xCB, tString);
		register(0xCC, tString, tByte, tByte, tByte, tBoolean);
		register(0xCD, tByte);
		register(0xCE, tString, tString, tByte);
		register(0xCF, tString, tByte, tString, tInt);
		register(0xD0, tByte, tString);
		register(0xD1, tString, tTeam);
		register(0xFA, tString, tShortByteArray);
		register(0xFC, tShortByteArray, tShortByteArray);
		register(0xFD, tString, tShortByteArray, tShortByteArray);
		register(0xFE, tByte);
		register(0xFF, tString);
		done();
	}
	
}
