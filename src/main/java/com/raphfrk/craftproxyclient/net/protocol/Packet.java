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

import org.bouncycastle.util.encoders.Hex;

public class Packet {
	
	protected final Object[] values;
	protected final byte[] arr;
	protected final int id;
	
	public Packet(Packet packet) {
		this.values = new Object[packet.values.length];
		System.arraycopy(packet.values, 0, this.values, 0, packet.values.length);
		this.arr = new byte[packet.arr.length];
		System.arraycopy(packet.arr, 0, this.arr, 0, packet.arr.length);
		this.id = packet.id;
	}
	
	public Packet(int id, Object[] values, ByteBuffer buf, int off, int len) {
		this.id = id;
		this.values = new Object[values.length ];
		System.arraycopy(values, 0, this.values, 0, values.length);
		this.arr = new byte[len];
		for (int i = 0; i < len; i++) {
			arr[i] = buf.get(i + off);
		}
	}
	
	public Packet(int id, Object[] values) {
		this.id = id;
		this.values = new Object[values.length];
		System.arraycopy(values, 0, this.values, 0, values.length);
		this.arr = null;
	}
	
	public int getId() {
		return id;
	}
	
	public Object getField(int id) {
		return values[id];
	}
	
	public void setField(int id, Object value) {
		values[id] = value;
	}
	
	public int getFieldCount() {
		return values.length;
	}
	
	public byte[] getSerialized() {
		return this.arr;
	}
	
	public int getLength() {
		return this.arr.length;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getSimpleName() + "{");
		boolean first = true;
		for (int i = 0; i < values.length; i++) {
			if (!first) {
				sb.append(", ");
			} else {
				first = false;
			}
			sb.append(values[i]);
		}
		if (!first) {
			sb.append(", ");
		} else {
			first = false;
		}
		sb.append(arr == null ? "<null>" : Hex.toHexString(arr));
		sb.append("}");
		return sb.toString();
	}

}
