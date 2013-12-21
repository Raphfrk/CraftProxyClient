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

import com.raphfrk.craftproxyclient.net.types.values.BulkData;

public class BulkDataType extends Type<BulkData> {

	public boolean writeRaw(BulkData data, ByteBuffer buf) {
		if (buf.remaining() >= data.getLength()) {
			int chunks = data.getChunks();
			buf.putShort((short) chunks);
			buf.putInt(data.getChunkData().length);
			buf.put((byte) (data.isSkylight() ? 1 : 0));
			buf.put(data.getChunkData());
			for (int i = 0; i < chunks; i++) {
				buf.putInt(data.getChunkX(i));
				buf.putInt(data.getChunkZ(i));
				buf.putShort(data.getBitmap(i));
				buf.putShort(data.getAdd(i));
			}
			return true;
		} else {
			System.out.println("failed to write bulk");
			return false;
		}
	}
	
	public static BulkData getRaw(ByteBuffer buf) {
		short chunks = buf.getShort();
		int length = buf.getInt();
		boolean skylight = buf.get() != 0;
		byte[] data = new byte[length];
		buf.get(data);
		int[] chunkX = new int[chunks];
		int[] chunkZ = new int[chunks];
		short[] bitmap = new short[chunks];
		short[] add = new short[chunks];
		for (int i = 0; i < chunks; i++) {
			chunkX[i] = buf.getInt();
			chunkZ[i] = buf.getInt();
			bitmap[i] = buf.getShort();
			add[i] = buf.getShort();
		}
		return new BulkData(data, chunkX, chunkZ, skylight, bitmap, add);
	}
	
	public static int getLengthRaw(ByteBuffer buf) {
		if (buf.remaining() < 6) {
			return -1;
		}
		short chunks = buf.getShort(buf.position());
		int dataLength = buf.getInt(buf.position() + 2);
		return 6 + chunks * 12 + dataLength + 1;
	}
	
	@Override
	public BulkData get(ByteBuffer buf) {
		return getRaw(buf);
	}

	@Override
	public int getLength(ByteBuffer buf) {
		return getLengthRaw(buf);
	}

	@Override
	public boolean write(BulkData data, ByteBuffer buf) {
		return writeRaw(data, buf);
	}

}
