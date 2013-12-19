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

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.json.simple.JSONObject;

import com.raphfrk.craftproxyclient.crypt.Crypt;
import com.raphfrk.craftproxyclient.gui.CraftProxyGUI;
import com.raphfrk.craftproxyclient.gui.GUIManager;

public class CraftProxyClient {
	
	private static CraftProxyClient instance;
	private final CraftProxyGUI gui;
	
	private CraftProxyClient() {
		gui = new CraftProxyGUI();
	}
	
	private void init() {
		
		if (!Crypt.init()) {
			GUIManager.messageBox("Unable to load Bouncy Castle crypt provider");
			return;
		}
		
		JSONObject loginDetails = GUIManager.getLoginDetails();
		
		if (loginDetails == null) {
			JOptionPane.showMessageDialog(null, "Login failed");
			closeGUI();
			return;
		} else {
			JOptionPane.showMessageDialog(null, "Login success");
		}
		
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					gui.setVisible(true);
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	private void closeGUI() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				getGUI().dispose();
			}
		});
	}

	public static void main(String[] args) {
		instance = new CraftProxyClient();
		instance.init();
	}

	public static CraftProxyClient getInstance() {
		return instance;
	}
	
	public static CraftProxyGUI getGUI() {
		return getInstance().gui;
	}

	
}
