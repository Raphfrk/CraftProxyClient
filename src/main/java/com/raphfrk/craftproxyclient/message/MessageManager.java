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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MessageManager {
	
	private final static String channel = "ChunkCache";
	
	public static String getChannelName() {
		return channel;
	}
	
	public static SubMessage decode(byte[] message) throws IOException {
		DataInputStream din = new DataInputStream(new ByteArrayInputStream(message));
		String subCommand = readString(16, din);
		int pos = 2 + (subCommand.length() << 1);
		byte[] data = new byte[message.length - pos];
		System.arraycopy(message, pos, data, 0, data.length);
		
		if (InitMessage.getSubCommandRaw().equals(subCommand)) {
			return new InitMessage(data);
		} else {
			return null;
		}
	}
	
	public static byte[] encode(String subCommand, byte[] data) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		writeString(subCommand, 16, dos);
		dos.write(data);
		dos.close();
		return bos.toByteArray();
	}
	
	public static void writeString(String s, int length, DataOutputStream dos) throws IOException {
		if (s.length() > length) {
			throw new IOException("String length exceeds maximum");
		}
		dos.writeShort(s.length());
		char[] chars = s.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			dos.writeChar(chars[i]);
		}
	}
	
	public static String readString(int maxLength, DataInputStream din) throws IOException {
		short length = din.readShort();
		if (length > maxLength) {
			throw new IOException("String length exceeds maximum");
		}
		char[] chars = new char[length];
		for (int i = 0; i < chars.length; i++) {
			chars[i] = din.readChar();
		}
		return new String(chars);
	}
	
	public static String[] splitRegisterData(byte[] data) {
		List<Integer> zeros = new ArrayList<Integer>();
		for (int i = 0; i < data.length; i++) {
			if (data[i] == 0) {
				zeros.add(i);
			}
		}
		zeros.add(data.length);
		List<String> channels = new ArrayList<String>();
		int last = -1;
		for (Integer zero : zeros) {
			int start = last + 1;
			int end = zero;
			if (start >= end - 1) {
				continue;
			}
			byte[] split = new byte[end - start];
			System.arraycopy(data, start, split, 0, split.length);
			channels.add(new String(split, StandardCharsets.UTF_8));
			last = zero;
		}
		return channels.toArray(new String[0]);
	}

}
