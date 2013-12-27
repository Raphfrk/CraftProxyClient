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
package com.raphfrk.craftproxyclient.crypt;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicReference;

public class Crypt {
	
	private static Crypt instance;
	
	private final static AtomicReference<SecureRandom> random = new AtomicReference<SecureRandom>();
	
	public static synchronized boolean init() {
		if (instance == null) {
			instance = new Crypt();
		}
		return true;
	}
	
	private static SecureRandom getSecureRandom() {
		SecureRandom r = random.getAndSet(null);
		if (r == null) {
			try {
				r = SecureRandom.getInstance("SHA1PRNG", "SUN");
			} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
				r = new SecureRandom();
			}
			
		}
		return r;
	}
	
	public static long getLong() {
		SecureRandom r = getSecureRandom();
		try {
			return r.nextLong();
		} finally {
			random.set(r);
		}
	}
	
	public static int getInt() {
		SecureRandom r = getSecureRandom();
		try {
			return r.nextInt();
		} finally {
			random.set(r);
		}
	}
	
	public static byte[] getBytes(int len) {
		byte[] arr = new byte[16];
		getBytes(arr);
		return arr;
	}
	
	public static void getBytes(byte[] arr) {
		SecureRandom r = getSecureRandom();
		try {
			r.nextBytes(arr);
		} finally {
			random.set(r);
		}
	}
	
}
