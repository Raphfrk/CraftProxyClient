package com.raphfrk.craftproxyclient.net;

public class ConnectionManager {
	
	public byte[] process(byte[] data) {
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) (~data[i]);
		}
		return data;
	}

}
