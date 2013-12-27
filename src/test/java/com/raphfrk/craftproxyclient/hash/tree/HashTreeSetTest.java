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
package com.raphfrk.craftproxyclient.hash.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.Test;

public class HashTreeSetTest {
	
	@Test
	public void test() {
		
		HashTreeSet set = new HashTreeSet();
		
		long a = 0x1234567812345678L;
		long b = 0x1234567800000000L;
		long c = 0x1234000000000000L;
		long d = 0x1200000000000000L;
		long e = 0x1235000000000000L;
		long f = 0x1236000000000000L;
		long g = Long.MIN_VALUE;
		long h = Long.MIN_VALUE + 1;
		long i = Long.MAX_VALUE;
		long j = Long.MAX_VALUE - 1;
		
		assertEquals("A should not be in set", -1, set.getSize(a));
		
		assertTrue("Adding A should return true", set.add(a));
		
		assertFalse("Adding A a second time should return false", set.add(a));
		
		assertEquals("A should have a length of one", 1, set.getSize(a));
		
		assertEquals("B should have a negative length, since not in set", -1, set.getSize(b));
		
		assertTrue("Adding B should return true", set.add(b));
		
		assertEquals("A should have a length of five", 5, set.getSize(a));

		assertEquals("B should have a length of five", 5, set.getSize(b));
		
		assertTrue("Adding C should return true", set.add(c));
		
		assertEquals("A should have a length of five", 5, set.getSize(a));

		assertEquals("B should have a length of five", 5, set.getSize(b));
		
		assertEquals("C should have a length of three", 3, set.getSize(c));
		
		assertTrue("Adding D should return true", set.add(d));
		
		assertEquals("A should have a length of five", 5, set.getSize(a));

		assertEquals("B should have a length of five", 5, set.getSize(b));
		
		assertEquals("C should have a length of three", 3, set.getSize(c));

		assertEquals("D should have a length of two", 2, set.getSize(d));
		
		assertTrue("Adding E should return true", set.add(e));

		assertTrue("Adding F should return true", set.add(f));
		
		assertEquals("A should have a length of five", 5, set.getSize(a));

		assertEquals("B should have a length of five", 5, set.getSize(b));
		
		assertEquals("C should have a length of three", 3, set.getSize(c));

		assertEquals("D should have a length of two", 2, set.getSize(d));
		
		assertEquals("E should have a length of two", 2, set.getSize(e));

		assertEquals("F should have a length of two", 2, set.getSize(f));
		
		assertTrue("Adding G should return true", set.add(g));

		assertTrue("Adding H should return true", set.add(h));

		assertTrue("Adding I should return true", set.add(i));

		assertTrue("Adding J should return true", set.add(j));

		assertEquals("A should have a length of five", 5, set.getSize(a));

		assertEquals("B should have a length of five", 5, set.getSize(b));
		
		assertEquals("C should have a length of three", 3, set.getSize(c));

		assertEquals("D should have a length of two", 2, set.getSize(d));
		
		assertEquals("E should have a length of two", 2, set.getSize(e));

		assertEquals("F should have a length of two", 2, set.getSize(f));
		
		assertEquals("G should have a length of two", 8, set.getSize(g));

		assertEquals("H should have a length of two", 8, set.getSize(h));
		
		assertEquals("I should have a length of two", 8, set.getSize(i));

		assertEquals("J should have a length of two", 8, set.getSize(j));
		
	}
	
	@Test
	public void singleWriteReadTest() throws IOException {
		
		HashTreeSet set = new HashTreeSet();
		
		ByteBuffer buf = ByteBuffer.allocate(9);
		
		long a = 0x2345678923456789L;
		
		assertTrue("Buffer write should return successfully", set.writeHash(buf, a));
		
		assertEquals("Nine bytes should have been written", 9, buf.position());
		
		buf.flip();
		
		assertEquals("Incorrect hash read", a, set.readHash(buf));
		
	}
	
	@Test
	public void multipleWriteReadTest() throws IOException {
		
		HashTreeSet set = new HashTreeSet();
		
		ByteBuffer buf = ByteBuffer.allocate(256 * 1024);

		Random r = new Random();
		
		int hashCount = 128;
		int loopCount = 65536;
		
		long hashes[] = new long[hashCount];
		long hashesSent[] = new long[loopCount];
		
		for (int i = 0; i < hashes.length; i++) {
			long h = r.nextLong();
			hashes[i] = h;
		}
		
		for (int i = 0; i < loopCount; i++) {
			int index = r.nextInt(Integer.MAX_VALUE) % hashCount;
			long h = hashes[index];
			hashesSent[i] = h;
			set.writeHash(buf, h);
		}
		
		buf.flip();
		
		HashTreeSet setRead = new HashTreeSet();
		
		for (int i = 0; i < loopCount; i++) {
			try {
				long hRead = setRead.readHash(buf);
				assertTrue("Hash mismatch at index " + i + " " + Long.toHexString(hashesSent[i]) + " != " + Long.toHexString(hRead), hashesSent[i] == hRead);
			} catch (IOException e) {
				System.out.println("index " + i);
				throw e;
			}
		}
	}

}
