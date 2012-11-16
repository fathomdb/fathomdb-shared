package com.fathomdb.hash;

import java.security.MessageDigest;

import com.fathomdb.utils.Hex;

public class Md5Hash extends StronglyTypedHash {
	private static final int MD5_BYTE_LENGTH = 128 / 8;
	public static final Hasher HASHER = new Hasher();

	public Md5Hash(String md5String) {
		this(Hex.fromHex(md5String));
	}

	public Md5Hash(byte[] md5) {
		super(md5);

		if (md5.length != MD5_BYTE_LENGTH) {
			throw new IllegalArgumentException();
		}
	}

	public static class Hasher extends HasherBase<Md5Hash> {
		public static final Hasher INSTANCE = new Hasher();

		@Override
		protected MessageDigest buildDigest() {
			return buildDigest("MD5");
		}

		@Override
		protected Md5Hash wrap(byte[] hash) {
			return new Md5Hash(hash);
		}
	}
}
