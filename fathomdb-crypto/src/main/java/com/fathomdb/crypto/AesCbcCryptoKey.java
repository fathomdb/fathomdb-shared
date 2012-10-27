package com.fathomdb.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;
import com.google.common.primitives.Bytes;

public class AesCbcCryptoKey extends CryptoKey {
	private static final Logger log = LoggerFactory.getLogger(AesCbcCryptoKey.class);

	private static final String CIPHER = "AES/CBC/PKCS5Padding";
	private static final int DEFAULT_KEYSIZE_BITS = 128;

	final SecretKey secret;

	private AesCbcCryptoKey(SecretKey secret) {
		this.secret = secret;
	}

	static final byte VERSION_1 = 0x78;

	@Override
	public byte[] decrypt(byte[] ciphertext) {
		Cipher cipher = getCipher(CIPHER);

		byte[] iv;

		byte version = ciphertext[0];
		if (version == VERSION_1) {
			int ivBytes = DEFAULT_KEYSIZE_BITS / 8;
			iv = Arrays.copyOfRange(ciphertext, 1, 1 + ivBytes);
			ciphertext = Arrays.copyOfRange(ciphertext, 1 + ivBytes, ciphertext.length);
		} else {
			throw new IllegalArgumentException();
			// log.warn("Found block without header (?)");
			// int ivBytes = DEFAULT_KEYSIZE_BITS / 8;
			// iv = Arrays.copyOfRange(ciphertext, 0, 0 + ivBytes);
			// ciphertext = Arrays.copyOfRange(ciphertext, 0 + ivBytes, ciphertext.length);
		}
		return decrypt(cipher, secret, iv, ciphertext);
	}

	@Override
	public byte[] encrypt(byte[] plaintext) {
		Cipher cipher = getCipher(CIPHER);

		int ivBytes = DEFAULT_KEYSIZE_BITS / 8;
		byte[] header = new byte[1];
		header[0] = VERSION_1;

		byte[] iv = FathomdbCrypto.generateSecureRandom(ivBytes);

		byte[] ciphertext = encrypt(cipher, secret, iv, plaintext);

		return Bytes.concat(header, iv, ciphertext);
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
