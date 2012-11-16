package com.fathomdb.hash;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.fathomdb.ImmutableBytes;
import com.fathomdb.Utf8;
import com.fathomdb.io.ByteSource;
import com.fathomdb.io.IoUtils;

public abstract class StronglyTypedHash extends ImmutableBytes {

	protected StronglyTypedHash(byte[] hash) {
		super(hash);
	}

	public static abstract class HasherBase<T extends StronglyTypedHash> {
		public T hash(String a) {
			return hash(a.getBytes(Utf8.CHARSET));
		}

		public T hash(byte[] data) {
			MessageDigest digest = buildDigest();

			byte[] hash = digest.digest(data);
			return wrap(hash);
		}

		protected abstract T wrap(byte[] hash);

		public T hash(File source) throws IOException {
			FileInputStream fis = new FileInputStream(source);
			try {
				return hash(fis);
			} finally {
				IoUtils.safeClose(fis);
			}
		}

		public T hash(ByteBuffer buffer) {
			MessageDigest digest = buildDigest();
			digest.update(buffer);
			byte[] hash = digest.digest();
			return wrap(hash);
		}

		public T hash(ByteSource data) throws IOException {
			InputStream is = data.open();
			try {
				return hash(is);
			} finally {
				IoUtils.safeClose(is);
			}
		}

		public T hash(InputStream is) throws IOException {
			MessageDigest digest = buildDigest();

			byte[] buffer = new byte[8192];
			while (true) {
				int available = is.read(buffer);
				if (available == -1) {
					break;
				}
				digest.update(buffer, 0, available);
			}
			byte[] hash = digest.digest();
			return wrap(hash);

		}

		protected abstract MessageDigest buildDigest();

		public static MessageDigest buildDigest(String name) {
			try {
				MessageDigest digest = MessageDigest.getInstance(name);
				return digest;
			} catch (NoSuchAlgorithmException e) {
				// should not happen
				throw new IllegalStateException("Could not find message digest algorithm: " + name, e);
			}
		}

	}

}
