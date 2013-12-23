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

import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressionManager {

	private static final ThreadLocal<Deflater> deflater = new ThreadLocal<Deflater>() {
		protected Deflater initialValue() {
			return new Deflater(6);
		}
	};
	
	private static final ThreadLocal<Inflater> inflater = new ThreadLocal<Inflater>() {
		protected Inflater initialValue() {
			return new Inflater();
		}
	};
	
	public static int deflate(byte[] input, byte[] output) {
		return deflate(input, output, 0, output.length);
	}
	
	public static int deflate(byte[] input, byte[] output, int off, int len) {
		Deflater d = deflater.get();
		d.reset();
		d.setInput(input);
		d.finish();
		
		int deflatedSize = d.deflate(output, off, len);
		d.reset();
		return deflatedSize;
	}
	
	public static int inflate(byte[] input, byte[] output) {
		return inflate(input, 0, input.length, output);
	}
	
	public static int inflate(byte[] input, int inOff, int inLen, byte[] output) {
		Inflater i = inflater.get();
		i.reset();
		i.setInput(input, inOff, inLen);
		i.finished();
		try {
			int inflatedSize = i.inflate(output);
			i.reset();
			return inflatedSize;
		} catch (DataFormatException e) {
			e.printStackTrace();
			return -1;
		}
	}

}
