package com.raphfrk.craftproxyclient.hash;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

public class HashStore {
	
	private TLongObjectMap<Hash> map = new TLongObjectHashMap<Hash>();

	public boolean hasKey(long hash) {
		return map.containsKey(hash);
	}
	
	// 512 entries per file 
	// variable hash size?
	// store files compressed
	
	public void add(Hash hash) {
		map.put(hash.getHash(), hash);
	}
	
	public Hash get(long hash) {
		return map.get(hash);
	}
	
}
