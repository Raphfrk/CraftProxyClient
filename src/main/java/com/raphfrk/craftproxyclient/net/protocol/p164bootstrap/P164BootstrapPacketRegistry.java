package com.raphfrk.craftproxyclient.net.protocol.p164bootstrap;

import com.raphfrk.craftproxyclient.net.protocol.Packet;
import com.raphfrk.craftproxyclient.net.protocol.PacketRegistry;
import com.raphfrk.craftproxyclient.net.protocol.p164.P164Handshake;
import com.raphfrk.craftproxyclient.net.protocol.p164.P164PacketRegistry;
import com.raphfrk.craftproxyclient.net.types.String16Type;
import com.raphfrk.craftproxyclient.net.types.Type;

public class P164BootstrapPacketRegistry extends PacketRegistry {
	
	private final static Type<String> tString = new String16Type();
	
	private final static PacketRegistry[] registry = new PacketRegistry[256];
	
	static {
		registry[78] = new P164PacketRegistry();
	}
	
	public P164BootstrapPacketRegistry() {
		super.register(2, new Type[] {tByte, tString, tString, tInt});
		super.register(0xFC, new Type[] {tShortByteArray, tShortByteArray});
		super.register(0xFD, new Type[] {tString, tShortByteArray, tShortByteArray});
		super.done();
	}
	
	public P164Handshake getHandshake(Packet p) {
		return new P164Handshake(p);
	}
	
	public PacketRegistry getRegistry(P164Handshake handshake) {
		return registry[handshake.getProtocolVersion()];
	}

}
