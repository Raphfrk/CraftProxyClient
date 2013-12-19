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
package com.raphfrk.craftproxyclient.gui;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.json.simple.JSONObject;

import com.raphfrk.craftproxyclient.CraftProxyClient;
import com.raphfrk.craftproxyclient.net.auth.AuthManager;

public class GUIManager {
	
	public static void messageBox(String message) {
		JOptionPane.showMessageDialog(CraftProxyClient.getGUI(), message);
	}
	
	public static JSONObject getLoginDetails() {
		JSONObject loginDetails = AuthManager.getLoginDetails();
		if (loginDetails != null) {
			return loginDetails;
		}
		loginDetails = getPreviousLoginDetails();
		if (loginDetails != null) {
			return loginDetails;
		}
		return getNewLoginDetails();
	}

	public static JSONObject getPreviousLoginDetails() {
		JSONObject loginInfo = AuthManager.refreshAccessToken();
		if (loginInfo == null) {
			return null;
		}
		int option = JOptionPane.showConfirmDialog(CraftProxyClient.getGUI(), "Login as " + AuthManager.getUsername() + "?", "Login", JOptionPane.YES_NO_OPTION);
		if (option == 0) {
			return loginInfo;
		} else {
			return null;
		}
	}

	public static JSONObject getNewLoginDetails() {
		JSONObject loginDetails = null;
		do {
			final LoginDialog login = new LoginDialog(CraftProxyClient.getGUI());
			login.setVisible(true);
			System.out.println("Thread " + Thread.currentThread());
			loginDetails = AuthManager.authAccessToken(login.getEmail(), login.getPassword());
			if (loginDetails == null) {
				System.out.println("Invalid login details");
			}
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					login.dispose();
				}
			});
			break;
		} while (loginDetails == null);
		
		return loginDetails;
	}

}
