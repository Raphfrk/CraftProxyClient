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
import java.util.ArrayList;

import com.raphfrk.craftproxyclient.net.types.values.Slot;

public class MetaType extends Type<Object[][]> {
	
	private final static int[] fixedLengths = new int[] {1, 2, 4, 4, -1, -1, 12};

	protected final static ByteType tByte = new ByteType();
	protected final static IntType tInt = new IntType();
	protected final static ShortType tShort = new ShortType();
	protected final static LongType tLong = new LongType();
	
	protected final static String16Type tString = new String16Type();

	protected final static SlotType tSlot = new SlotType();
	
	public boolean writeRaw(Object[][] data, ByteBuffer buf) {
		if (buf.remaining() >= getLengthRaw(buf)) {
			Object[] indexes = data[0];
			Object[] values = data[1];
			for (int i = 0; i < indexes.length; i++) {
				int item = (Integer) indexes[0];
				int type = item >> 5;
				buf.put((byte) item);
				
				if (type == 0) {
					tByte.writeRaw((Byte) values[i], buf);
				} else if (type == 1) {
					tShort.writeRaw((Short) values[i], buf);
				} else if (type == 2) {
					tInt.writeRaw((Integer) values[i], buf);
				} else if (type == 3) {
					tInt.writeRaw((Integer) values[i], buf);
				} else if (type == 4) {
					tString.writeRaw((String) values[i], buf);
				} else if (type == 5) {
					tSlot.writeRaw((Slot) values[i], buf);
				} else if (type == 6) {
					int[] vector = (int[]) values[i];
					for (int j = 0; j < 3; j++) {
						tInt.write(vector[j], buf);
					}
				}
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	public static Object[][] getRaw(ByteBuffer buf) {
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		ArrayList<Object> values = new ArrayList<Object>();
		int item = 0;
		while (item != 127) {
			item = buf.get() & 0xFF;
			if (item == 127) {
				continue;
			}
			int type = item >> 5;
			indexes.add(item);
			if (type == 0) {
				values.add(ByteType.getRaw(buf));
			} else if (type == 1) {
				values.add(ShortType.getRaw(buf));
			} else if (type == 2) {
				values.add(IntType.getRaw(buf));
			} else if (type == 3) {
				values.add(IntType.getRaw(buf));
			} else if (type == 4) {
				values.add(String16Type.getRaw(buf));
			} else if (type == 5) {
				values.add(SlotType.getRaw(buf));
			} else if (type == 6) {
				int[] vector = new int[3];
				for (int i = 0; i < 3; i++) {
					vector[i] = IntType.getRaw(buf);
				}
				values.add(vector);
			} else {
				values.add(null);
			}
		}
		Object[][] meta = new Object[2][];
		meta[0] = indexes.toArray();
		meta[1] = values.toArray();
		return meta;
	}
	
	public static int getLengthRaw(ByteBuffer buf) {
		int pos = buf.position();
		try {
			int length = 0;
			int item = 0;
			while (item != 127) {
				if (!buf.hasRemaining()) {
					return -1;
				}
				item = buf.get() & 0xFF;
				int type = item >> 5;
				length++;
				if (item == 127) {
					return length;
				} else if (type >= 0 && type < 7) {
					int fixed = fixedLengths[type];
					int dataLength = 0;
					if (fixed > 0) {
						dataLength = fixed;
					} else {
						if (type == 4) {
							dataLength = String16Type.getLengthRaw(buf);
						} else if (type == 5) {
							dataLength = SlotType.getLengthRaw(buf);
						}
						if (dataLength == -1) {
							return -1;
						}
					}
					if (buf.remaining() >= dataLength) {
						buf.position(buf.position() + dataLength);
						length += dataLength;
					} else {
						return -1;
					}
				}
			}
			return length;
		} finally {
			buf.position(pos);
		}
	}
	
	@Override
	public Object[][] get(ByteBuffer buf) {
		return getRaw(buf);
	}

	@Override
	public int getLength(ByteBuffer buf) {
		return getLengthRaw(buf);
	}

	@Override
	public boolean write(Object[][] data, ByteBuffer buf) {
		return writeRaw(data, buf);
	}

}
