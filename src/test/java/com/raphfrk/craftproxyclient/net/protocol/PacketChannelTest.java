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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Random;

import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import com.raphfrk.craftproxyclient.net.types.ByteSizedByteArrayType;
import com.raphfrk.craftproxyclient.net.types.IntType;
import com.raphfrk.craftproxyclient.net.types.LongType;
import com.raphfrk.craftproxyclient.net.types.ShortType;
import com.raphfrk.craftproxyclient.net.types.Type;

public class PacketChannelTest {
	
	@Test
	public void transferTest() throws IOException {
		
		PacketRegistry registry = new PacketRegistry()
			.register(0, new Type[] {new IntType(), new ByteSizedByteArrayType(), new ShortType(), new IntType()})
			.register(1, new Type[] {new LongType()})
			.done();

		byte[] arr = new byte[128];
		int pos = 0;
		
		Random r = new Random();
		
		while (true) {
			int l;
			if (r.nextBoolean()) {
				l = addPacketZero(arr, pos);
			} else {
				l = addPacketOne(arr, pos);
			}
			if (l == -1) {
				break;
			}
			pos = l;
		}
		
		ReadableByteChannel readable = new ReadableByteChannelImpl(arr, pos);
		WritableByteChannelImpl writable = new WritableByteChannelImpl();
		
		ByteChannelImpl combined = new ByteChannelImpl(readable, writable);
		
		PacketChannel channel = new PacketChannel(combined, 640, registry);
		
		try {
			while (true) {
				int id = channel.getPacketId();
				assertTrue("Unexpected packet id " + id, id == 0 || id == 1);
				channel.TransferPacket(writable);
			}
		} catch (EOFException e) {
		}
		
		byte[] output = writable.toByteArray();

		assertTrue("Unexpected output length " + output.length, output.length == pos);
		for (int i = 0; i < pos; i++) {
			assertTrue("Unexpected output at position " + i, output[i] == arr[i]);
		}
		
	}
	
	@Test
	public void loopDiscardTest() throws IOException {
		for (int i = 0; i < 512; i++) {
			discardTest();
		}
	}
	
	public void discardTest() throws IOException {
		
		PacketRegistry registry = new PacketRegistry()
			.register(0, new Type[] {new IntType(), new ByteSizedByteArrayType(), new ShortType(), new IntType()})
			.register(1, new Type[] {new LongType()})
			.done();

		byte[] arr = new byte[128];
		int pos = 0;
		
		Random r = new Random();
		
		while (true) {
			int l;
			if (r.nextBoolean()) {
				l = addPacketZero(arr, pos);
			} else {
				l = addPacketOne(arr, pos);
			}
			if (l == -1) {
				break;
			}
			pos = l;
		}
		
		ReadableByteChannel readable = new ReadableByteChannelImpl(arr, pos);
		WritableByteChannelImpl writable = new WritableByteChannelImpl();
		
		ByteChannelImpl combined = new ByteChannelImpl(readable, writable);
		
		PacketChannel channel = new PacketChannel(combined, 64, registry);
		channel.setWrappedChannel(channel.getRawChannel());
		
		try {
			while (true) {
				int id = channel.getPacketId();
				assertTrue("Unexpected packet id " + id, id == 0 || id == 1);
				channel.TransferPacket(writable);
			}
		} catch (EOFException e) {
		}
		
		byte[] output = writable.toByteArray();

		assertTrue("Unexpected output length " + output.length, output.length == pos);
		for (int i = 0; i < pos; i++) {
			assertTrue("Unexpected output at position " + i, output[i] == arr[i]);
		}
		
	}
	
	@Test
	public void skipTest() throws IOException {

		PacketRegistry registry = new PacketRegistry()
		.register(0, new Type[] {new IntType(), new ByteSizedByteArrayType(), new ShortType(), new IntType()})
		.register(1, new Type[] {new LongType()})
		.done();

		byte[] arr = new byte[256];

		int l = 0;
		for (int i = 0; i < 5; i++) {
			l = addPacketZero(arr, l);
			assertTrue("Buffer filled", l != -1);
		}
		for (int i = 0; i < 5; i++) {
			l = addPacketOne(arr, l);
			assertTrue("Buffer filled", l != -1);
		}
		
		ReadableByteChannel readable = new ReadableByteChannelImpl(arr, l);
		
		ByteChannelImpl combined = new ByteChannelImpl(readable, null);
		
		PacketChannel channel = new PacketChannel(combined, 64, registry);
		
		for (int i = 0; i < 5; i++) {
			int id = channel.getPacketId();
			assertEquals("Unexcepted packet id", 0, id);
			channel.skipPacket();
		}
		for (int i = 0; i < 5; i++) {
			assertEquals("Unexcepted packet id", 1, channel.getPacketId());
			channel.skipPacket();
		}
		
		boolean thrown = false;
		try {
			channel.getPacketId();
		} catch (EOFException e) {
			thrown = true;
		}
		assertTrue("No EOF exception thrown", thrown);
		
	}
	
	@Test
	public void markTest() throws IOException {

		PacketRegistry registry = new PacketRegistry()
		.register(0, new Type[] {new IntType(), new ByteSizedByteArrayType(), new ShortType(), new IntType()})
		.register(1, new Type[] {new LongType()})
		.done();

		byte[] arr = new byte[256];

		int l = 0;
		for (int i = 0; i < 5; i++) {
			l = addPacketZero(arr, l);
			assertTrue("Buffer filled", l != -1);
		}
		for (int i = 0; i < 5; i++) {
			l = addPacketOne(arr, l);
			assertTrue("Buffer filled", l != -1);
		}
		
		ReadableByteChannel readable = new ReadableByteChannelImpl(arr, l);
		
		ByteChannelImpl combined = new ByteChannelImpl(readable, null);
		
		PacketChannel channel = new PacketChannel(combined, 256, registry);
		
		for (int i = 0; i < 3; i++) {
			int id = channel.getPacketId();
			assertEquals("Unexcepted packet id", 0, id);
			channel.skipPacket();
		}
		
		channel.mark();
		
		for (int i = 0; i < 2; i++) {
			int id = channel.getPacketId();
			assertEquals("Unexcepted packet id", 0, id);
			channel.skipPacket();
		}
		
		for (int i = 0; i < 3; i++) {
			assertEquals("Unexcepted packet id", 1, channel.getPacketId());
			channel.skipPacket();
		}
		
		channel.reset();
		
		for (int i = 0; i < 2; i++) {
			int id = channel.getPacketId();
			assertEquals("Unexcepted packet id", 0, id);
			channel.skipPacket();
		}
		
		for (int i = 0; i < 5; i++) {
			assertEquals("Unexcepted packet id", 1, channel.getPacketId());
			channel.skipPacket();
		}
		
		boolean thrown = false;
		try {
			channel.getPacketId();
		} catch (EOFException e) {
			thrown = true;
		}
		assertTrue("No EOF exception thrown", thrown);
		
	}
	
	@Test
	public void getPacketTest() throws IOException {

		PacketRegistry registry = new PacketRegistry()
		.register(0, new Type[] {new IntType(), new ByteSizedByteArrayType(), new ShortType(), new IntType()})
		.register(1, new Type[] {new LongType()})
		.done();

		byte[] arr = new byte[256];

		int l = 0;
		for (int i = 0; i < 5; i++) {
			l = addPacketZero(arr, l);
			assertTrue("Buffer filled", l != -1);
		}
		for (int i = 0; i < 5; i++) {
			l = addPacketOne(arr, l);
			assertTrue("Buffer filled", l != -1);
		}
		
		ReadableByteChannel readable = new ReadableByteChannelImpl(arr, l);
		
		ByteChannelImpl combined = new ByteChannelImpl(readable, null);
		
		PacketChannel channel = new PacketChannel(combined, 64, registry);
		
		for (int i = 0; i < 5; i++) {
			int id = channel.getPacketId();
			assertEquals("Unexcepted packet id", 0, id);
			Packet p = channel.getPacket();
			assertTrue("Opcode not a byte", p.getField(0).getClass().equals(Byte.class));
			assertTrue("First field not an Integer", p.getField(1).getClass().equals(Integer.class));
			assertTrue("Second field not an array", p.getField(2).getClass().isArray());
			assertTrue("Second field not a byte array", p.getField(2).getClass().getComponentType().equals(byte.class));
			assertTrue("Third field not an Short", p.getField(3).getClass().equals(Short.class));
			assertTrue("Fourth field not an Integer", p.getField(4).getClass().equals(Integer.class));
		}
		for (int i = 0; i < 5; i++) {
			assertEquals("Unexcepted packet id", 1, channel.getPacketId());
			Packet p = channel.getPacket();
			assertTrue("Opcode not a byte", p.getField(0).getClass().equals(Byte.class));
			assertTrue("First field not an Integer", p.getField(1).getClass().equals(Long.class));		
		}
		
		boolean thrown = false;
		try {
			channel.getPacketId();
		} catch (EOFException e) {
			thrown = true;
		}
		assertTrue("No EOF exception thrown", thrown);
		
	}
	
	@Test
	public void writePacketTest() throws IOException {

		PacketRegistry registry = new PacketRegistry()
		.register(0, new Type[] {new IntType(), new ByteSizedByteArrayType(), new ShortType(), new IntType()})
		.register(1, new Type[] {new LongType()})
		.done();

		byte[] arr = new byte[256];

		int l = 0;
		
		ReadableByteChannel readable = new ReadableByteChannelImpl(arr, l);
		WritableByteChannelImpl writable = new WritableByteChannelImpl();
		
		ByteChannelImpl combined = new ByteChannelImpl(readable, writable);
		
		PacketChannel channel = new PacketChannel(combined, 64, 64, registry);

		Packet p1 = new Packet(1, new Object[] {(byte) 1, 7L});
		Packet p0 = new Packet(0, new Object[] {(byte) 0, 9, new byte[] {0, 1, -1}, (short) 12, 0x12345678});
		
		channel.writePacket(p1);
		channel.writePacket(p0);
		
		ByteBuffer buf = ByteBuffer.wrap(writable.toByteArray());

		assertTrue("Incorrect opcode for first packet", buf.get() == 1);
		assertTrue("Incorrect long data for first packet", buf.getLong() == 7L);
		assertTrue("Incorrect opcode for second packet", buf.get() == 0);
		assertTrue("Incorrect int data for second packet", buf.getInt() == 9);
		assertTrue("Incorrect byte array length for second packet", buf.get() == 3);
		assertTrue("Incorrect byte array data[0] for second packet", buf.get() == 0);
		assertTrue("Incorrect byte array data[1] for second packet", buf.get() == 1);
		assertTrue("Incorrect byte array data[2] for second packet", buf.get() == -1);
		assertTrue("Incorrect short data for second packet", buf.getShort() == 12);
		assertTrue("Incorrect int data for second packet", buf.getInt() == 0x12345678);
	
	}

	public int addPacketZero(byte[] arr, int pos) {
		int remaining = arr.length - pos;
		if (remaining < 12) {
			return -1;
		}
		Random r = new Random();
		int len = r.nextInt(5);
		if (len > remaining - 12) {
			len = remaining - 12;
		}
		arr[pos++] = 0;
		pos = nextBytes(r, arr, pos, 4);
		arr[pos++] = (byte) len;
		pos = nextBytes(r, arr, pos, len);
		pos = nextBytes(r, arr, pos, 2);
		pos = nextBytes(r, arr, pos, 4);
		return pos;
	}
	
	public int addPacketOne(byte[] arr, int pos) {
		int remaining = arr.length - pos;
		if (remaining < 9) {
			return -1;
		}
		Random r = new Random();
		
		arr[pos++] = 1;
		pos = nextBytes(r, arr, pos, 8);
		return pos;
	}
	
	private int nextBytes(Random r, byte[] arr, int pos, int length) {
		for (int i = 0; i < length; i++) {
			arr[pos++] = (byte) r.nextInt();
		}
		return pos;
	}
	
}
