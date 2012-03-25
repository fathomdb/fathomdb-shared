package com.fathomdb.proxy.cache;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.fathomdb.proxy.crypto.Md5Digest;
import com.fathomdb.proxy.utils.Hex;

public class StrongHash {
	final byte[] data;

	public static final int SIZE = 128 / 8;

	public static final StrongHash ZERO = new StrongHash(new byte[SIZE]);

	private StrongHash(byte[] data) {
		if (data.length != SIZE)
			throw new IllegalStateException();
		this.data = data;
	}

	public static StrongHash read(ByteBuffer buffer) {
		byte[] data = new byte[SIZE];
		buffer.get(data);
		return new StrongHash(data);
	}

	public static StrongHash computeHash(ByteBuffer buffer) {
		Md5Digest digest = new Md5Digest();
		byte[] hash = digest.hash(buffer);
		return new StrongHash(hash);
	}

	public void put(ByteBuffer buffer) {
		buffer.put(data);
	}

	@Override
	public String toString() {
		return Hex.toHex(data);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(data);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StrongHash other = (StrongHash) obj;
		if (!Arrays.equals(data, other.data))
			return false;
		return true;
	}

}
