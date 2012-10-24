package com.fathomdb.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Closeables;

public class FathomdbCrypto {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(FathomdbCrypto.class);

	private static final byte ALGORITHM_AES = 1;
	private static final byte AES_DEPRECATED = 1;
	private static final byte AES_CBC = 2;

	private static final int RAW_SALT_LENGTH = 16;

	private static final int LEGACY_ITERATION_COUNT = 1000;

	public static byte[] serialize(CryptoKey secret) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte algorithm;
			byte version;
			if (secret instanceof AesCryptoKey) {
				algorithm = ALGORITHM_AES;
				version = AES_DEPRECATED;
			} else if (secret instanceof AesCbcCryptoKey) {
				algorithm = ALGORITHM_AES;
				version = AES_CBC;
			} else {
				throw new IllegalArgumentException();
			}

			baos.write(algorithm);
			baos.write(version);

			secret.write(baos);

			baos.close();

			return baos.toByteArray();
		} catch (IOException e) {
			throw new IllegalArgumentException("Error serializing key", e);
		}
	}

	public static CryptoKey deserializeKey(byte[] data) {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		try {
			if (data.length == (AesCryptoKey.DEFAULT_KEYSIZE_BITS / 8)) {
				return AesCryptoKey.readRaw(bais);
			}

			int algorithm = bais.read();
			int version = bais.read();

			switch (algorithm) {
			case ALGORITHM_AES:
				switch (version) {
				case AES_DEPRECATED:
					return AesCryptoKey.read(bais);

				case AES_CBC:
					return AesCbcCryptoKey.read(bais);
				}
			}

			throw new IllegalArgumentException("Invalid serialized key");
		} catch (IOException e) {
			throw new IllegalArgumentException("Invalid serialized key", e);
		} finally {
			Closeables.closeQuietly(bais);
		}
	}

	public static byte[] decrypt(CryptoKey secret, byte[] iv, byte[] ciphertext) {
		return secret.decrypt(iv, ciphertext);
	}

	public static byte[] decrypt(CryptoKey secret, byte[] ciphertext) {
		return decrypt(secret, null, ciphertext);
	}

	public static byte[] encrypt(CryptoKey secret, byte[] iv, byte[] plaintext) {
		return secret.encrypt(iv, plaintext);
	}

	public static byte[] encrypt(CryptoKey secret, byte[] plaintext) {
		return encrypt(secret, null, plaintext);
	}

	public static CryptoKey deriveKey(byte[] salt, String password) {
		if (salt.length == RAW_SALT_LENGTH) {
			// Legacy value
			int iterationCount = LEGACY_ITERATION_COUNT;
			return AesCryptoKey.deriveKey(iterationCount, salt, password);
		}

		byte algorithm = salt[0];
		byte version = salt[1];
		byte iterationCountCode = salt[2];

		int iterationCount;
		switch (iterationCountCode) {
		case 1:
			iterationCount = 1000;
			break;

		default:
			throw new IllegalArgumentException();
		}

		byte[] tail = Arrays.copyOfRange(salt, 3, salt.length);
		switch (algorithm) {
		case ALGORITHM_AES:
			switch (version) {
			case AES_DEPRECATED:
				return AesCryptoKey.deriveKey(iterationCount, tail, password);

			case AES_CBC:
				return deriveKeyRaw(iterationCount, tail, password);
			}
		}

		throw new IllegalArgumentException("Invalid serialized key");
	}

	public static CryptoKey deriveKeyRaw(int iterationCount, byte[] salt, String password) {
		return AesCbcCryptoKey.deriveKey(iterationCount, salt, password);
	}

	public static CryptoKey generateKey() {
		return AesCbcCryptoKey.generateKey();
	}

	static final SecureRandom SECURE_RANDOM = new SecureRandom();

	public static byte[] generateSecureRandom(int length) {
		synchronized (SECURE_RANDOM) {
			byte[] data = new byte[length];
			SECURE_RANDOM.nextBytes(data);
			return data;
		}
	}
}
