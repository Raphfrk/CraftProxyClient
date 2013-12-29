package com.raphfrk.craftproxyclient.net.protocol.p17x;

import com.raphfrk.craftproxyclient.net.protocol.Packet;

public class P17xLoginStart extends Packet {

	public P17xLoginStart(Packet p) {
		super(p);
	}
	
	public String getUsername() {
		return (String) getField(2);
	}

}
