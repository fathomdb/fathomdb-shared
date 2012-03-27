package com.fathomdb.proxy.cache;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class HashKey {
	final byte[] key;
	final int hash;

	public HashKey(byte[] key) {
		this.key = key;
		this.hash = Arrays.hashCode(key);
	}

	public int size() {
		return key.length;
	}

	public void put(ByteBuffer buffer) {
		if (key.length > Short.MAX_VALUE)
			throw new IllegalStateException();
		short keyLength = (short) key.length;
		buffer.putShort(keyLength);
		buffer.put(key);
	}

	public static HashKey get(ByteBuffer buffer) {
		int byteCount = buffer.getShort();
		if (byteCount <= 0)
			throw new IllegalStateException("Corrupt byte count in entry");
		byte[] keyData = new byte[byteCount];
		buffer.get(keyData);

		return new HashKey(keyData);
	}

	// TODO: Is this needed or does the JVM do this for us?
	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HashKey other = (HashKey) obj;
		if (hash != other.hash)
			return false;
		if (!Arrays.equals(key, other.key))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "HashKey [key=" + new String(key) + "]";
	}

	
	
}
