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
package com.raphfrk.craftproxyclient.net.auth;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.raphfrk.craftproxyclient.crypt.Crypt;

public class AuthManager {
	
	private final static String authServer = "https://authserver.mojang.com";
	private final static String sessionServer = "http://session.minecraft.net/game/joinserver.jsp?";
	private final static String tokenFilename = "access-token.1.64.json";

	private static JSONObject loginDetails;
	
	public static JSONObject getLoginDetails() {
		return loginDetails;
	}
	
	public static JSONObject refreshAccessToken() {
		
		JSONObject obj = readAccessToken();
		
		JSONObject stripped = stripLoginDetails(obj, false);
		if (stripped == null) {
			return null;
		}
		
		JSONObject reply = sendRequest(stripped, "refresh");
		
		stripped = stripLoginDetails(reply, true);
		if (stripped == null) {
			return null;
		}
		
		writeAccessToken(stripped);
		
		AuthManager.loginDetails = stripped;
		
		return reply;
		
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject authAccessToken(String email, String password) {

		String clientToken = null;
		
		JSONObject fileObj = readAccessToken();
		
		if (fileObj != null) {
			clientToken = (String) fileObj.get("clientToken");
		}
		
		if (clientToken == null) {
			byte[] arr = new byte[32];
			Crypt.getBytes(arr);
			clientToken = UUID.nameUUIDFromBytes(arr).toString();
		}
		
		JSONObject obj = new JSONObject();
		
		JSONObject agent = new JSONObject();
		agent.put("name", "Minecraft");
		agent.put("version", 1);
		
		obj.put("agent", agent);
		
		obj.put("username", email);
		obj.put("password", password);
		obj.put("clientToken", clientToken);
		
		JSONObject reply = sendRequest(obj, "authenticate");
		
		if (reply == null) {
			return null;
		}
		
		JSONObject stripped = stripLoginDetails(reply, true);
		if (stripped == null) {
			return null;
		}
		
		writeAccessToken(stripped);
		
		AuthManager.loginDetails = stripped;
		
		return reply;
	}
	
	public static JSONObject sendRequest(JSONObject request, String endpoint) {
		URL url;
		try {
			url = new URL(authServer + "/" + endpoint);
		} catch (MalformedURLException e) {
			return null;
		}
		try {
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setInstanceFollowRedirects(false);
			con.setReadTimeout(5000);
			con.setConnectTimeout(5000);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json"); 
			con.connect();

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(con.getOutputStream(), StandardCharsets.UTF_8));
			try {
				request.writeJSONString(writer);
				writer.flush();
				writer.close();
				
				if (con.getResponseCode() != 200) {
					return null;
				}

				BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
				try {
					JSONParser parser = new JSONParser();

					try {
						return (JSONObject) parser.parse(reader);
					} catch (ParseException e) {
						return null;
					}
				} finally {
					reader.close();
				}
			} finally {
				writer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public static String getUsername() {
		if (loginDetails == null) {
			return null;
		}
		JSONObject profile = (JSONObject) loginDetails.get("selectedProfile");
		
		return (String) profile.get("name");
	}
	
	public static String getAccessToken() {
		if (loginDetails == null) {
			return null;
		}
		return (String) loginDetails.get("accessToken");
	}
	
	public static void authServer(String hash) throws IOException {
		URL url;
		try {
			if (loginDetails == null) {
				throw new IOException("Not logged in");
			}

			String username;
			String accessToken;
			try {
				username = URLEncoder.encode(getUsername(), "UTF-8");
				accessToken = URLEncoder.encode(getAccessToken(), "UTF-8");
				hash = URLEncoder.encode(hash, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new IOException("Username/password encoding error", e);
			}

			String urlString = sessionServer + "user=" + username + "&sessionId=" + accessToken + "&serverId=" + hash;
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			throw new IOException("Auth server URL error", e);
		}
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setInstanceFollowRedirects(false); 
		con.setReadTimeout(5000);
		con.setConnectTimeout(5000);
		con.connect();

		if (con.getResponseCode() != 200) {
			throw new IOException("Auth server rejected username and password");
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
		try {
			String reply = reader.readLine();
			if (!"OK".equals(reply)) {
				throw new IOException("Auth server replied (" + reply + ")");
			}
		} finally {
			reader.close();
		}
	}
	
	private static JSONObject readAccessToken() {
		Reader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(tokenFilename), StandardCharsets.UTF_8));
			JSONParser parser = new JSONParser();
			return (JSONObject) parser.parse(reader);
		} catch (IOException | ParseException e) {
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}
	
	private static void writeAccessToken(JSONObject obj) {
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tokenFilename), StandardCharsets.UTF_8));
			obj.writeJSONString(writer);
			
		} catch (IOException e) {
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static JSONObject stripLoginDetails(JSONObject obj, boolean includeUsername) {
		if (obj == null) {
			return null;
		}
		
		String clientToken = (String) obj.get("clientToken");
		String accessToken = (String) obj.get("accessToken");
		
		if (clientToken == null || accessToken == null) {
			return null;
		}
		
		JSONObject selectedProfile = (JSONObject) obj.get("selectedProfile");
		
		if (selectedProfile == null) {
			return null;
		}
		
		String username = (String) selectedProfile.get("name");
		if (username == null) {
			return null;
		}
		JSONObject stripped = new JSONObject();
		
		stripped.put("accessToken", accessToken);
		stripped.put("clientToken", clientToken);
		
		if (includeUsername) {
			JSONObject selectedProfileNew = new JSONObject();
			selectedProfileNew.put("name", username);

			stripped.put("selectedProfile", selectedProfileNew);
		}
		return stripped;
		
	}


}
