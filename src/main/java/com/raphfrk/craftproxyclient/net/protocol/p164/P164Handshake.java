package com.raphfrk.craftproxyclient.net.protocol.p164;

import com.raphfrk.craftproxyclient.net.protocol.Handshake;
import com.raphfrk.craftproxyclient.net.protocol.Packet;

public class P164Handshake extends Packet implements Handshake {

	public P164Handshake(Packet p) {
		super(p);
	}
	
	public int getProtocolVersion() {
		return ((Byte) getField(1)) & 0xFF;
	}
	
	public String getUsername() {
		return (String) getField(2);
	}
	
	public String getServerhost() {
		return (String) getField(3);
	}
	
	public int getServerPort() {
		return (Integer) getField(4);
	}

}
