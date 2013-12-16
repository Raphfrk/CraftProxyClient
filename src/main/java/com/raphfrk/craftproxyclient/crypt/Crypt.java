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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class Crypt {
	
	private static Crypt instance;
	
	private final static String filename = "bcprov-jdk15on-150.jar";
	
	private final static AtomicReference<SecureRandom> random = new AtomicReference<SecureRandom>();
	
	public static synchronized boolean init() {
		if (instance == null) {
			try {
				instance = new Crypt();
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException e) {
				return false;
			}
		}
		return true;
	}
	
	private Crypt() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {

		copyBCJar();
		
		loadBCProvider();
	}
	
	private void loadBCProvider() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		Path p = Paths.get(filename);

		URLClassLoader ucl = URLClassLoader.newInstance(new URL[] {p.toUri().toURL()}, getClass().getClassLoader());

		Class<?> clazz = ucl.loadClass("org.bouncycastle.jce.provider.BouncyCastleProvider");

		Security.addProvider((Provider) clazz.newInstance());

	}
	
	private void copyBCJar() throws IOException {
		File f = new File(filename);
		
		if (f.exists()) {
			return;
		}
		
		URL u = getBCJar();
		
		if (u == null) {
			throw new IOException("Bouncy castle not present in jar file");
		}
		
		copyFromURL(u, f);
	}
	
	private URL getBCJar() {
		return getClass().getResource("/" + filename);
	}
	
	private void copyFromURL(URL src, File dest) throws IOException{
		InputStream in = src.openStream();
		
		try {
			OutputStream out = new FileOutputStream(filename);
			try {
				byte[] buf = new byte[2048];
				int count = 0;
				while (count != -1) {
					count = in.read(buf);
					if (count > 0) {
						out.write(buf, 0, count);
					}
				}
			} finally {
				if (out != null) {
					out.close();
				}
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
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
	
	public static void getBytes(byte[] arr) {
		SecureRandom r = getSecureRandom();
		try {
			r.nextBytes(arr);
		} finally {
			random.set(r);
		}
	}
	
}
