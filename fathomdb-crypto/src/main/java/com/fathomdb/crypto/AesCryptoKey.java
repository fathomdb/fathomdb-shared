package com.fathomdb.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

/**
 * Deprecated: Doesn't use an IV, and is ECB mode
 * 
 * @author justinsb
 * 
 */
@Deprecated
public class AesCryptoKey extends CryptoKey {
	private static final Logger log = LoggerFactory.getLogger(AesCryptoKey.class);

	final SecretKey secret;

	private static final String CIPHER = "AES";
	public static final int DEFAULT_KEYSIZE_BITS = 128;

	private AesCryptoKey(SecretKey secret) {
		log.warn("Using insecure AES mode");
		this.secret = secret;
	}

	@Override
	public byte[] decrypt(byte[] ciphertext) {

		Cipher cipher = getCipher(CIPHER);

		byte[] iv = null; // Deprecated
		return decrypt(cipher, secret, iv, ciphertext);
	}

	@Override
	public byte[] encrypt(byte[] plaintext) {
		Cipher cipher = getCipher(CIPHER);

		byte[] iv = null; // Deprecated
		return encrypt(cipher, secret, iv, plaintext);
	}

	@Override
	public ByteBuffer encrypt(ByteBuffer plaintext) {
		throw new UnsupportedOperationException();
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
