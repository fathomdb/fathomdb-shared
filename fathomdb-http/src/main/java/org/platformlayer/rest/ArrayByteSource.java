package org.platformlayer.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.fathomdb.io.ByteSourceBase;

public class ArrayByteSource extends ByteSourceBase {
	public static final ArrayByteSource EMPTY = new ArrayByteSource(new byte[0]);
	private final byte[] bytes;

	public ArrayByteSource(byte[] bytes) {
		this.bytes = bytes;
	}

	@Override
	public InputStream open() throws IOException {
		return new ByteArrayInputStream(bytes);
	}

	@Override
	public long getContentLength() {
		return bytes.length;
	}

	@Override
	public void close() throws IOException {
	}
}
