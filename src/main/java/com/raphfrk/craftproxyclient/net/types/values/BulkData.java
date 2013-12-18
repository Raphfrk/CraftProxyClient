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

import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.bouncycastle.util.Arrays;

public class BulkData {
	
	private final byte[] chunkData;
	private final int[] chunkX;
	private final int[] chunkZ;
	private final short[] bitmap;
	private final short[] add;
	
	public BulkData(byte[] chunkData, int[] chunkX, int[] chunkZ, short[] bitmap, short[] add) {
		this.chunkData = ByteUtils.clone(chunkData);
		this.chunkX = Arrays.copyOf(chunkX, chunkX.length);
		this.chunkZ = Arrays.copyOf(chunkZ, chunkZ.length);
		this.bitmap = new short[bitmap.length];
		System.arraycopy(bitmap, 0, this.bitmap, 0, bitmap.length);
		this.add = new short[add.length];
		System.arraycopy(bitmap, 0, this.add, 0, add.length);
	}

	public byte[] getChunkData() {
		return chunkData;
	}
	
	public int getChunkX(int i) {
		return chunkX[i];
	}
	
	public int getChunkZ(int i) {
		return chunkZ[i];
	}
	
	public short getBitmap(int i) {
		return bitmap[i];
	}
	
	public short getAdd(int i) {
		return add[i];
	}
}
