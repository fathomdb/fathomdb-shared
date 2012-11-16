package com.fathomdb.hash;

import java.security.MessageDigest;

import com.fathomdb.utils.Hex;

public class Sha1Hash extends StronglyTypedHash {
	private static final int SHA1_BYTE_LENGTH = 160 / 8;
	public static final Hasher HASHER = new Hasher();

	public Sha1Hash(String md5String) {
		this(Hex.fromHex(md5String));
	}

	public Sha1Hash(byte[] md5) {
		super(md5);

		if (md5.length != SHA1_BYTE_LENGTH) {
			throw new IllegalArgumentException();
		}
	}

	public static class Hasher extends HasherBase<Sha1Hash> {
		public static final Hasher INSTANCE = new Hasher();

		@Override
		protected MessageDigest buildDigest() {
			return buildDigest("SHA-1");
		}

		@Override
		protected Sha1Hash wrap(byte[] hash) {
			return new Sha1Hash(hash);
		}

	}
}
