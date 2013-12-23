package com.raphfrk.craftproxyclient.message;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Random;

import org.junit.Test;

import com.raphfrk.craftproxyclient.hash.Hash;

public class HashDataMessageTest {
	
	@Test
	public void encodeDecode() throws IOException {

		Hash[] hashes = new Hash[5];
		
		Random r = new Random();
		for (int i = 0; i < hashes.length; i++) {
			byte[] bytes = new byte[4096];
			r.nextBytes(bytes);
			hashes[i] = new Hash(bytes, 2048, 2048);
		}
		
		HashDataMessage message = new HashDataMessage(hashes, 0, hashes.length);
		
		byte[] encoded = MessageManager.encode(message);
		
		HashDataMessage decoded = (HashDataMessage) MessageManager.decode(encoded);
		
		assertEquals(message, decoded);
		
	}

}
