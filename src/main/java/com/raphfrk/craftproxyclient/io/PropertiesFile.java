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
/*******************************************************************************
 * Copyright (C) 2012 Raphfrk
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
 ******************************************************************************/
package com.raphfrk.craftproxyclient.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class PropertiesFile {

	static final String slash = System.getProperty("file.separator");

	private final HashMap<String, String> map;

	private final String filename;

	public PropertiesFile(String filename) {
		this.filename = filename;
		map = new HashMap<String, String>();
	}

	public boolean containsKey(String name) {
		return map.containsKey(name);
	}

	public void load() throws IOException {

		String[] lines = fileToString(filename);

		if (lines == null) {
			return;
		}

		for (String line : lines) {
			if (line.indexOf('#') == -1) {
				String[] split = line.split("=", 2);
				if (split.length == 2) {
					map.put(split[0], split[1]);
				}
			}
		}
	}

	public void save() throws IOException {

		ArrayList<String> strings = new ArrayList<String>();

		Iterator<Entry<String, String>> itr = map.entrySet().iterator();
		while (itr.hasNext()) {
			Entry<String, String> current = itr.next();
			strings.add(current.getKey() + "=" + current.getValue());
		}

		stringToFile(strings, filename);

	}

	public int getInt(String name) {
		return Integer.parseInt(map.get(name));
	}

	public long getLong(String name) {
		return Long.parseLong(map.get(name));
	}

	public boolean getBoolean(String name) {
		return Boolean.parseBoolean(map.get(name));
	}

	public double getDouble(String name) {
		return Double.parseDouble(map.get(name));
	}

	public String getString(String name) {
		return new String(map.get(name));
	}

	public int getInt(String name, int defaultValue) {
		if (!map.containsKey(name)) {
			map.put(name, Integer.toString(defaultValue));
			return defaultValue;
		}
		return Integer.parseInt(map.get(name));
	}

	public long getLong(String name, long defaultValue) {
		if (!map.containsKey(name)) {
			map.put(name, Long.toString(defaultValue));
			return defaultValue;
		}
		return Long.parseLong(map.get(name));
	}

	public boolean getBoolean(String name, boolean defaultValue) {
		if (!map.containsKey(name)) {
			map.put(name, Boolean.toString(defaultValue));
			return defaultValue;
		}
		return Boolean.parseBoolean(map.get(name));
	}

	public double getDouble(String name, double defaultValue) {
		if (!map.containsKey(name)) {
			map.put(name, Double.toString(defaultValue));
			return defaultValue;
		}
		return Double.parseDouble(map.get(name));
	}

	public String getString(String name, String defaultValue) {
			if (!map.containsKey(name)) {
				map.put(name, new String(defaultValue));
				return defaultValue;
			}
			return new String(map.get(name));
	}

	public void setInt(String name, int value) {
		map.put(name, Integer.toString(value));
	}

	public void setLong(String name, long value) {
		map.put(name, Long.toString(value));
	}

	public void setDouble(String name, double value) {
		map.put(name, Double.toString(value));
	}

	public void setBoolean(String name, boolean value) {
		map.put(name, Boolean.toString(value));
	}

	public void setString(String name, String value) {
		map.put(name, new String(value));
	}

	private static String[] fileToString(String filename) throws IOException {

		File file = new File(filename);

		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(file));

			StringBuffer sb = new StringBuffer();

			String line;

			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}

			return (sb.toString().split("\n"));

		} finally {
			if (br != null) {
				br.close();
			}
		}

	}

	private static void stringToFile(ArrayList<String> string, String filename) throws IOException {

		File file = new File(filename);

		BufferedWriter bw = null;

		try {
			bw = new BufferedWriter(new FileWriter(file));

			for (Object line : string.toArray()) {
				bw.write((String) line);
				bw.newLine();
			}
		} finally {
			if (bw != null) {
				bw.close();
			}
		}

	}
}
