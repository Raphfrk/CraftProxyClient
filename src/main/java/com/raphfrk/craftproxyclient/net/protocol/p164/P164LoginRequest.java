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
package com.raphfrk.craftproxyclient.net.protocol.p164;

import com.raphfrk.craftproxyclient.net.protocol.Packet;

public class P164LoginRequest extends Packet {
	
	public P164LoginRequest(Packet p) {
		super(p);
	}
	
	public P164LoginRequest(int entityId, String worldType, byte gamemode, byte dimension, byte difficulty, byte none, byte maxPlayers) {
		super(0x01, new Object[] {(byte) 0x01, entityId, worldType, gamemode, dimension, difficulty, none, maxPlayers});
	}
	
	
	public int getEntityId() {
		return (Integer) getField(1);
	}
	
	public String getWorldType() {
		return (String) getField(2);
	}
	
	public byte getGameMode() {
		return (Byte) getField(3);
	}
	
	public byte getDimension() {
		return (byte) getField(4);
	}
	
	public byte getDifficulty() {
		return (byte) getField(5);
	}
	
	public byte getNone() {
		return (byte) getField(6);
	}
	
	public byte getMaxPlayers() {
		return (byte) getField(7);
	}

}
