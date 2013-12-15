package com.raphfrk.craftproxyclient.net.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class ByteChannelImpl implements ByteChannel {

	private final ReadableByteChannel readChannel;
	private final WritableByteChannel writeChannel;
	
	public ByteChannelImpl(ReadableByteChannel readChannel, WritableByteChannel writeChannel) {
		this.readChannel = readChannel;
		this.writeChannel = writeChannel;
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		return readChannel.read(dst);
	}

	@Override
	public void close() throws IOException {
		try {
			readChannel.close();
		} finally {
			writeChannel.close();
		}
	}

	@Override
	public boolean isOpen() {
		return readChannel.isOpen() || writeChannel.isOpen();
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		return writeChannel.write(src);
	}
	
}
