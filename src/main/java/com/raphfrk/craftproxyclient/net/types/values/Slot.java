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
package com.raphfrk.craftproxyclient.net.types.values;

public class Slot {
	
	private final short id;
	private final byte count;
	private final short damage;
	private final byte[] nbt;
	
	public Slot() {
		this((short) -1, (byte) 0, (short) 0);
	}
	
	public Slot(short id, byte count, short damage) {
		this(id, count, damage, new byte[0]);
	}
	
	public Slot(short id, byte count, short damage, byte[] nbt) {
		this.id = id;
		this.count = count;
		this.damage = damage;
		this.nbt = new byte[nbt.length];
		System.arraycopy(nbt, 0, this.nbt, 0, nbt.length);
	}
	
	public Slot(Slot slot) {
		this(slot.id, slot.count, slot.damage, slot.nbt);
	}
	
	public int length() {
		if (id == -1) {
			return 2;
		} else {
			return 7 + nbt.length;
		}
	}
	
	public short getId() {
		return id;
	}
	
	public byte getCount() {
		return count;
	}
	
	public short getDamage() {
		return damage;
	}
	
	public byte[] getNBT() {
		return nbt;
	}

}
