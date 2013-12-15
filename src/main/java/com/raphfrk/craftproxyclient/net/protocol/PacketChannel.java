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

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import com.raphfrk.craftproxyclient.net.types.Type;

public class PacketChannel {
	
	private final ReadableByteChannel channel;
	private final ByteBuffer buf;
	private final PacketRegistry registry;
	
	private int id = -1;
	private int read = 0;
	private int mark = -1;
	private int packetStart = -1;
	
	public PacketChannel(ReadableByteChannel channel, int bufferSize, PacketRegistry registry) {
		this.channel = channel;
		this.buf = ByteBuffer.allocateDirect(bufferSize);
		this.registry = registry;
		this.setReading();
	}
	
	/**
	 * Gets the next packet
	 * 
	 * @return
	 * @throws IOException 
	 */
	public Packet getPacket() throws IOException {
		packetStart = buf.position();
		getPacketId();
		@SuppressWarnings("rawtypes")
		Type[] types = registry.getPacketInfo(id);
		if (types == null) {
			throw new IOException("Unable to process packet id " + id);
		}

		Object[] values = new Object[types.length];
		
		int totalLength = 0;
		for (int i = 0; i < types.length; i++) {
			int length = hardGetLength(types[i]);
			if (buf.remaining() < length) {
				readBytesToBuffer(length - buf.remaining());
			}
			values[i] = types[i].get(buf);
			totalLength += length;
		}
		Packet packet = registry.getPacket(id, values, buf, packetStart, totalLength);
		id = -1;
		return packet;
	}
	
	/**
	 * Transfers the next packet to a channel
	 * 
	 * @param channel the destination channel
	 * @throws IOException
	 */
	public void TransferPacket(WritableByteChannel channel) throws IOException {
		packetStart = buf.position();
		getPacketId();
		@SuppressWarnings("rawtypes")
		Type[] types = registry.getCompressedPacketInfo(id);
		if (types == null) {
			throw new IOException("Unable to process packet id " + id);
		}
		int limit = buf.limit();

		for (int i = 0; i < types.length; i++) {
			int length = hardGetLength(types[i]);
			limit = buf.limit();
			while (length > 0) {
				int newLimit = buf.position() + length;
				if (newLimit < limit) {
					buf.limit(newLimit);
				}
				length -= channel.write(buf);
				if (buf.remaining() == 0 && length > 0) {
					buf.limit(limit);
					readBytesToBuffer(1);
					limit = buf.limit();
				}
			}
			buf.limit(limit);
		}
		id = -1;
	}
	
	/**
	 * Skips the next packet
	 * 
	 * @throws IOException
	 */
	public void skipPacket() throws IOException {
		packetStart = buf.position();
		getPacketId();
		@SuppressWarnings("rawtypes")
		Type[] types = registry.getCompressedPacketInfo(id);
		if (types == null) {
			throw new IOException("Unable to process packet id " + id);
		}

		for (int i = 0; i < types.length; i++) {
			int length = hardGetLength(types[i]);
			if (buf.remaining() < length) {
				readBytesToBuffer(length - buf.remaining());
			}
			buf.position(buf.position() + length);
		}
		id = -1;
	}
	
	/**
	 * Gets the id of the next packet
	 * 
	 * @return
	 * @throws IOException
	 */
	public int getPacketId() throws IOException {
		if (id == -1) {
			if (buf.remaining() == 0) {
				readBytesToBuffer(1);
			}
			id = buf.get(buf.position()) & 0xFF;
		}
		return id;
	}
	
	/**
	 * Marks the current location.  Only one mark may be active at a time
	 * 
	 * @throws IOException
	 */
	public void mark() throws IOException {
		mark = buf.position();
	}
	
	/**
	 * Discards the current mark
	 */
	public void discard() {
		if (mark == -1) {
			throw new IllegalStateException("No mark set");
		}
		mark = -1;
	}
	
	/**
	 * Resets the channel to the current mark
	 */
	public void reset() {
		if (mark == -1) {
			throw new IllegalStateException("Cannot discard since no mark set");
		}
		id = -1;
		buf.position(mark);
	}
	
	private int hardGetLength(Type<?> type) throws IOException {
		int length = -1;
		length = type.getLength(buf);
		while (length == -1) {
			readBytesToBuffer(1);
			length = type.getLength(buf);
		}
		return length;
	}
	
	private void compact() {
		int m = getEffectiveMark();
		if (m == -1) {
			throw new IllegalStateException("Cannot compact since no mark set");
		}
		int offset = buf.position() - m;
		buf.position(m);
		buf.compact();
		buf.flip();

		buf.position(offset);
		if (mark != -1) {
			mark -= m;
		}
		packetStart -= m;
	}
	
	private void readBytesToBuffer(int bytes) throws IOException {
		setWriting();
		try {
			while (bytes > 0) {
				int r = channel.read(buf);
				printBuffer(buf);
				bytes -= r;
				if (r == 0 && bytes > 0 && buf.remaining() == 0) {
					if (getEffectiveMark() > 0) {
						setReading();
						try {
							compact();
						} finally {
							setWriting();
						}

					} else {
						throw new IOException("PacketSocketChannel buffer full");
					}
				}
				if (r == -1 && bytes > 0) {
					throw new EOFException("EOF in channel reached");
				}

			}
		} finally {
			setReading();
		}
	}
	
	private static void printBuffer(ByteBuffer buf) {
		byte[] temp = new byte[buf.capacity()];
		for (int i = 0; i < buf.capacity(); i++) {
			temp[i] = buf.get(i);
		}
	}

	private void setWriting() {
		read = buf.position();
		buf.position(buf.limit());
		buf.limit(buf.capacity());
	}
	
	private void setReading() {
		buf.limit(buf.position());
		buf.position(read);
	}
	
	private int getEffectiveMark() {
		return mark == -1 ? packetStart : mark;
	}

}