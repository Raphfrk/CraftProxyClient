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
package com.raphfrk.craftproxyclient.net.types;

import java.nio.ByteBuffer;

public abstract class Type<T> {
	
	/**
	 * Gets the length of the type, or -1, if if there is insufficient data in the 
	 * buffer to determine the length
	 * 
	 * @param channel
	 * @param buf
	 */
	public abstract int getLength(ByteBuffer buf);
	
	/**
	 * Writes the data to the buffer
	 * 
	 * @param data
	 * @param buf
	 * @return true if the there was sufficient space to write the data
	 */
	public abstract boolean write(T data, ByteBuffer buf);
	
	/**
	 * Decodes an instance of this type from the buffer
	 * 
	 * @param buf
	 * @return
	 */
	public abstract T get(ByteBuffer buf);
	
	/**
	 * Gets the size of the type, or -1 for variable sized types
	 * 
	 * @return
	 */
	public int getFixedSize() {
		return -1;
	}
	
	protected static void putByte(short b, ByteBuffer buf) {
		buf.put((byte) b);
	}
	
	protected static void putByte(int b, ByteBuffer buf) {
		buf.put((byte) b);
	}
	
	protected static void putByte(long b, ByteBuffer buf) {
		buf.put((byte) b);
	}
	
	protected static int getByte(ByteBuffer buf) {
		return buf.get() & 0xFF;
	}

}
