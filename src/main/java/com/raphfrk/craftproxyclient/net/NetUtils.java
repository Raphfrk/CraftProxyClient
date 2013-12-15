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
package com.raphfrk.craftproxyclient.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class NetUtils {

	public static int readVarInt(InputStream in) throws IOException {
		
		/*int x = 0;
		
		int shift = 0;
		
		for (int i = 0; i < 10; i++) {
			int d = in.read();
			if (d == -1) {
				throw new EOFException("");
			}
			byte b = (byte) d;
			x |= (b & 0x7f) << (shift);
			shift += 7;
			if (b < 0) {
				return x;
			}
		}*/

		throw new IOException("Varint out of range");
		
	}
	
	public static void writeVarInt(OutputStream out, int x) throws IOException {
		
		for (int i = 0; i < 10; i++) {
			byte b = (byte) (x & 0x7f);
			x >>= 7;
			if (x == 0) {
				b |= (byte) 0x80;
			}
			out.write(b);
			if (x == 0) {
				return;
			}
		}
		
		throw new IOException("Varint out of range"); // should be impossible
		
	}
	
}
