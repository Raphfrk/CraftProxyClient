package com.raphfrk.craftproxyclient.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public class SingleByteByteChannelWrapper implements ByteChannel {
	
	private final ByteChannel channel;
	
	public SingleByteByteChannelWrapper(ByteChannel channel) {
		this.channel = channel;
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		if (!dst.hasRemaining()) {
			return 0;
		}
		int limit = dst.limit();
		dst.limit(dst.position() + 1);
		int r = channel.read(dst);
		dst.limit(limit);
		return r;
	}

	@Override
	public void close() throws IOException {
		channel.close();
	}

	@Override
	public boolean isOpen() {
		return channel.isOpen();
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		if (!src.hasRemaining()) {
			return 0;
		}
		int limit = src.limit();
		src.limit(src.position() + 1);
		int w = channel.write(src);
		src.limit(limit);
		return w;
	}

}
