package com.fathomdb.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.io.ByteStreams;

public class AesCbcCryptoKey extends CryptoKey {
	private static final String CIPHER = "AES/CBC/PKCS5Padding";
	private static final int DEFAULT_KEYSIZE_BITS = 128;

	final SecretKey secret;

	private AesCbcCryptoKey(SecretKey secret) {
		this.secret = secret;
	}

	@Override
	public byte[] decrypt(byte[] iv, byte[] ciphertext) {
		Cipher cipher = getCipher(CIPHER);

		return decrypt(cipher, secret, iv, ciphertext);
	}

	@Override
	public byte[] encrypt(byte[] iv, byte[] plaintext) {
		Cipher cipher = getCipher(CIPHER);

		return encrypt(cipher, secret, iv, plaintext);
	}

	static AesCbcCryptoKey read(InputStream is) throws IOException {
		int length = is.read();
		if ((length * 8) != DEFAULT_KEYSIZE_BITS) {
			throw new IllegalArgumentException();
		}
		byte[] keyData = new byte[length];
		ByteStreams.readFully(is, keyData);

		SecretKeySpec key = new SecretKeySpec(keyData, CIPHER);
		return new AesCbcCryptoKey(key);
	}

	@Override
	void write(OutputStream os) throws IOException {
		byte[] keyData = secret.getEncoded();
		os.write(keyData.length);
		os.write(keyData);
	}

	static AesCbcCryptoKey generateKey() {
		SecretKey secret = generateKey("AES", DEFAULT_KEYSIZE_BITS);
		return new AesCbcCryptoKey(secret);
	}

	static AesCbcCryptoKey deriveKey(int iterationCount, byte[] salt, String password) {
		PBEKey pbeKey = KeyDerivationFunctions.doPbkdf2(iterationCount, salt, password, DEFAULT_KEYSIZE_BITS);
		SecretKey secretKey = new SecretKeySpec(pbeKey.getEncoded(), "AES");
		return new AesCbcCryptoKey(secretKey);
	}
}
