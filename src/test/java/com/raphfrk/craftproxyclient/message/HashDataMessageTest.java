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
package com.raphfrk.craftproxyclient.message;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Random;

import org.junit.Test;

import com.raphfrk.craftproxyclient.hash.Hash;

public class HashDataMessageTest {
	
	@Test
	public void encodeDecode() throws IOException {

		Hash[] hashes = new Hash[5];
		
		Random r = new Random();
		for (int i = 0; i < hashes.length; i++) {
			byte[] bytes = new byte[4096];
			r.nextBytes(bytes);
			hashes[i] = new Hash(bytes, 2048, 2048);
		}
		
		HashDataMessage message = new HashDataMessage(hashes, 0, hashes.length);
		
		byte[] encoded = MessageManager.encode(message);
		
		HashDataMessage decoded = (HashDataMessage) MessageManager.decode(encoded);
		
		assertEquals(message, decoded);
		
	}

}
