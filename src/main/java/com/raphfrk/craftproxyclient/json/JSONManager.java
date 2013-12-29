package com.raphfrk.craftproxyclient.json;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONObject;

public class JSONManager {
	
	public static byte[] JSONToBytes(JSONObject obj) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		OutputStreamWriter wos = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
		BufferedWriter br = new BufferedWriter(wos);
		try {
			obj.writeJSONString(br);
			br.close();
		} catch (IOException e) {
			return null;
		}
		return bos.toByteArray();
	}

}
