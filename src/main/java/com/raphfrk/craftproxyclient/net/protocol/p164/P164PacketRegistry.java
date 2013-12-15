package com.raphfrk.craftproxyclient.net.protocol.p164;

import com.raphfrk.craftproxyclient.net.protocol.Handshake;
import com.raphfrk.craftproxyclient.net.protocol.PacketRegistry;
import com.raphfrk.craftproxyclient.net.types.String16Type;
import com.raphfrk.craftproxyclient.net.types.Type;

public class P164PacketRegistry extends PacketRegistry {
	
	private final static Type<String> tString = new String16Type();
	
	public P164PacketRegistry() {
		super.register(2, new Type[] {tByte, tString, tString, tInt});
		super.register(0xFC, new Type[] {tShortByteArray, tShortByteArray});
		super.register(0xFD, new Type[] {tString, tShortByteArray, tShortByteArray});
		super.done();
	}
	
	@Override
	public void handleLogin(Handshake handshake) {
		P164Handshake h = (P164Handshake) handshake;
		
	}

}
