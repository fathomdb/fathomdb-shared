package com.fathomdb.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

@Deprecated
public class AesCryptoKey extends CryptoKey {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AesCryptoKey.class);

	final SecretKey secret;

	private static final String CIPHER = "AES";
	public static final int DEFAULT_KEYSIZE_BITS = 128;

	private AesCryptoKey(SecretKey secret) {
		log.warn("Using insecure AES mode");
		this.secret = secret;
	}

	@Override
	public byte[] decrypt(byte[] iv, byte[] ciphertext) {
		// This is why this is deprecated, along with ECB mode
		iv = null;

		Cipher cipher = getCipher(CIPHER);

		return decrypt(cipher, secret, iv, ciphertext);
	}

	@Override
	public byte[] encrypt(byte[] iv, byte[] plaintext) {
		// This is why this is deprecated, along with ECB mode
		iv = null;

		Cipher cipher = getCipher(CIPHER);

		return encrypt(cipher, secret, iv, plaintext);
	}

	static AesCryptoKey read(InputStream is) throws IOException {
		int length = is.read();
		if ((length * 8) != DEFAULT_KEYSIZE_BITS) {
			throw new IllegalArgumentException();
		}
		byte[] keyData = new byte[length];
		ByteStreams.readFully(is, keyData);

		SecretKeySpec key = new SecretKeySpec(keyData, CIPHER);
		return new AesCryptoKey(key);
	}

	static AesCryptoKey readRaw(InputStream is) throws IOException {
		byte[] keyData = new byte[DEFAULT_KEYSIZE_BITS / 8];
		ByteStreams.readFully(is, keyData);

		SecretKeySpec key = new SecretKeySpec(keyData, CIPHER);
		return new AesCryptoKey(key);
	}

	@Override
	void write(OutputStream os) throws IOException {
		byte[] keyData = secret.getEncoded();
		os.write(keyData.length);
		os.write(keyData);
	}

	static CryptoKey deriveKey(int iterationCount, byte[] salt, String password) {
		PBEKey pbeKey = KeyDerivationFunctions.doPbkdf2(iterationCount, salt, password, DEFAULT_KEYSIZE_BITS);
		SecretKey secretKey = new SecretKeySpec(pbeKey.getEncoded(), CIPHER);
		return new AesCryptoKey(secretKey);
	}
}
