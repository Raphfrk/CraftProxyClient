package com.raphfrk.craftproxyclient.net.protocol.p164bootstrap;

import com.raphfrk.craftproxyclient.net.protocol.Handshake;
import com.raphfrk.craftproxyclient.net.protocol.Packet;
import com.raphfrk.craftproxyclient.net.protocol.Protocol;
import com.raphfrk.craftproxyclient.net.protocol.p164.P164Handshake;
import com.raphfrk.craftproxyclient.net.protocol.p164.P164Protocol;

public class P164BootstrapProtocol extends Protocol {
	
	private final static Protocol[] protocol = new Protocol[256];
	
	static {
		protocol[78] = new P164Protocol();
	}

	public P164BootstrapProtocol() {
		super(new P164BootstrapPacketRegistry());
	}
	
	public P164Handshake getHandshake(Packet p) {
		return new P164Handshake(p);
	}
	
	public Protocol getProtocol(Handshake handshake) {
		return protocol[((P164Handshake) handshake).getProtocolVersion()];
	}

}
