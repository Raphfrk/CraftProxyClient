package com.raphfrk.craftproxyclient.net.protocol.p164;

import com.raphfrk.craftproxyclient.net.protocol.Packet;

public class P164EncryptionKeyResponse extends Packet {
	
	public P164EncryptionKeyResponse(Packet p) {
		super(p);
	}
	
	public P164EncryptionKeyResponse(int id, byte[] secret, byte[] token) {
		super(0xFC, new Object[] {secret, token});
	}

}
