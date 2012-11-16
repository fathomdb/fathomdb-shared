package com.fathomdb;

import java.util.Arrays;

import com.fathomdb.utils.Hex;

@Deprecated
// Use protobuf ByteString
public class ImmutableBytes {
	final byte[] bytes;

	public ImmutableBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public int size() {
		return bytes.length;
	}

	public byte[] toByteArray() {
		return Arrays.copyOf(bytes, bytes.length);
	}

	public String toHex() {
		return Hex.toHex(bytes);
	}

	@Override
	public String toString() {
		return "ByteString [" + Hex.toHex(bytes) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(bytes);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ImmutableBytes other = (ImmutableBytes) obj;
		if (!Arrays.equals(bytes, other.bytes)) {
			return false;
		}
		return true;
	}

}
