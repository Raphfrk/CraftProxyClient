package com.raphfrk.craftproxyclient.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

import org.bouncycastle.crypto.BufferedBlockCipher;

public class CryptByteChannelWrapper implements ByteChannel {
	
	private final ByteChannel channel;
	private final ByteBuffer readByteBuffer;
	private final byte[] readBufferIn;
	private final byte[] readBufferOut;
	private final ByteBuffer writeByteBuffer;
	private final byte[] writeBufferIn;
	private final byte[] writeBufferOut;
	private final BufferedBlockCipher out;
	private final BufferedBlockCipher in;
	
	public CryptByteChannelWrapper(ByteChannel channel, BufferedBlockCipher out, BufferedBlockCipher in) {
		this.channel = channel;
		this.out = out;
		this.in = in;
		this.readByteBuffer = ByteBuffer.allocateDirect(32);
		this.writeByteBuffer = ByteBuffer.allocateDirect(32);
		this.readBufferIn = new byte[32];
		this.writeBufferIn = new byte[32];
		this.readBufferOut = new byte[32];
		this.writeBufferOut = new byte[32];
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		if (!dst.hasRemaining()) {
			return 0;
		}
		readByteBuffer.clear();
		readByteBuffer.limit(Math.min(dst.remaining(), readByteBuffer.capacity()));
		int r = channel.read(readByteBuffer);
		if (r == -1) {
			return -1;
		}
		readByteBuffer.flip();
		readByteBuffer.get(readBufferIn, 0, r);
		int d = in.processBytes(readBufferIn, 0, r, readBufferOut, 0);
		if (d != r) {
			throw new IOException("Block cipher does not act as a one to one byte converter, " + r + " written but " + d + " produced");
		}
		dst.put(readBufferOut, 0, r);
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
		
		int written = 0;
		while (src.hasRemaining()) {
			int w = Math.min(src.remaining(), writeBufferIn.length);

			src.get(writeBufferIn, 0, w);
			
			int e = out.processBytes(writeBufferIn, 0, w, writeBufferOut, 0);
			if (e != w) {
				throw new IOException("Block cipher does not act as a one to one byte converter, " + w + " written but " + e + " produced");
			}
			
			writeByteBuffer.clear();
			writeByteBuffer.put(writeBufferOut, 0, w);
			writeByteBuffer.flip();
			while (w > 0) {
				int ww = channel.write(writeByteBuffer);
				if (ww == -1) {
					return -1;
				}
				w -= ww;
				written += ww;
			}
		}
		return written;
	}

}
