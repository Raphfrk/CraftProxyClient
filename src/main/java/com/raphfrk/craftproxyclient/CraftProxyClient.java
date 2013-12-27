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
package com.raphfrk.craftproxyclient;

import java.io.File;
import java.io.IOException;

import javax.swing.SwingUtilities;

import com.raphfrk.craftproxyclient.crypt.Crypt;
import com.raphfrk.craftproxyclient.gui.CraftProxyGUI;
import com.raphfrk.craftproxyclient.gui.GUIManager;
import com.raphfrk.craftproxyclient.io.HashFileStore;

public class CraftProxyClient {
	
	private static CraftProxyClient instance;
	private final CraftProxyGUI gui;
	
	private CraftProxyClient() {
		gui = new CraftProxyGUI();
	}
	
	private boolean init() {
		
		if (!Crypt.init()) {
			GUIManager.messageBox("Unable to load Bouncy Castle crypt provider");
			return false;
		}
		
		try {
			HashFileStore.lockDirectory(new File("cache"));
		} catch (IOException e) {
			GUIManager.messageBox(e.getMessage());
			return false;
		}
		
		gui.init();
		
		return true;
		
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				instance = new CraftProxyClient();
				if (!instance.init()) {
					instance.gui.dispose();
				}
			}
		});
	}

	public static CraftProxyClient getInstance() {
		return instance;
	}
	
	public static CraftProxyGUI getGUI() {
		return getInstance().gui;
	}

	
}
