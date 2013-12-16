package com.raphfrk.craftproxyclient.net.auth;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.raphfrk.craftproxyclient.crypt.Crypt;

public class AuthManager {
	
	private final static String authServer = " https://authserver.mojang.com";
	private final static String tokenFilename = "access-token.1.64.json";

	public static JSONObject refreshAccessToken() {
		
		JSONObject obj = readAccessToken();
		
		if (obj == null) {
			System.out.println("Unable to read");
			return null;
		}
		
		String clientToken = (String) obj.get("clientToken");
		String accessToken = (String) obj.get("accessToken");
		
		if (clientToken == null || accessToken == null) {
			System.out.println("Tag missing");
			return null;
		}
		
		JSONObject reply = sendRequest(obj, "refresh");
		
		System.out.println(reply);
		
		if (reply == null) {
			return null;
		}
		
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
		
		System.out.println("Reply: " + reply);
		
		if (reply == null) {
			return null;
		}
		
		String accessToken = (String) reply.get("accessToken");
		
		writeAccessToken(clientToken, accessToken);
		
		return reply;
	}

	
	public static JSONObject sendRequest(JSONObject request, String endpoint) {
		System.out.println("Attempting request " + request);
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
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json"); 
			con.connect();

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(con.getOutputStream(), StandardCharsets.UTF_8));
			try {
				request.writeJSONString(writer);
				writer.flush();
				writer.close();
				
				System.out.println("Response code " + con.getResponseCode());
				
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
	
	@SuppressWarnings("unchecked")
	private static void writeAccessToken(String clientToken, String accessToken) {
		Writer writer = null;
		try {
			JSONObject obj = new JSONObject();
			obj.put("clientToken", clientToken);
			obj.put("accessToken", accessToken);
			
			System.out.println("Writing " + obj);
			
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

}
