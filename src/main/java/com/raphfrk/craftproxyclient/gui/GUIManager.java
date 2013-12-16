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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

import org.json.simple.JSONObject;

import com.raphfrk.craftproxyclient.net.auth.AuthManager;

public class GUIManager {
	
	public static void messageBox(String message) {
		JOptionPane.showMessageDialog(null, message);
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
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		try {
			System.out.println("Do you want to reuse info " + loginInfo);
			String reply = input.readLine();
			if (reply.startsWith("y") || reply.startsWith("Y")) {
				return loginInfo;
			} else {
				return null;
			}
		} catch (IOException e) {
			return null;
		}
	}

	public static JSONObject getNewLoginDetails() {
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		try {
			JSONObject loginDetails = null;
			do {
				System.out.println("Please enter email address");
				String email = input.readLine();
				System.out.println("Please enter password");
				String password = input.readLine();
				loginDetails = AuthManager.authAccessToken(email, password);
				if (loginDetails == null) {
					System.out.println("Invalid login details");
				}
			} while (loginDetails == null);
			return loginDetails;
		} catch (IOException e) {
			return null;
		}
	}

}
