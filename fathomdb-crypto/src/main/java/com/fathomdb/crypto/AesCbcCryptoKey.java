package com.fathomdb.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.io.ByteStreams;
import com.google.common.primitives.Bytes;

public class AesCbcCryptoKey extends CryptoKey {
	private static final String ALGORITHM = "AES";
	private static final String CIPHER = "AES/CBC/PKCS5Padding";
	private static final int DEFAULT_KEYSIZE_BITS = 128;

	final SecretKey secret;
	private final int keysizeBits;

	private AesCbcCryptoKey(SecretKey secret, int keysizeBits) {
		this.secret = secret;
		this.keysizeBits = keysizeBits;
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

		int ivBytes = keysizeBits / 8;
		byte[] header = new byte[1];
		header[0] = VERSION_1;

		byte[] iv = FathomdbCrypto.generateSecureRandom(ivBytes);

		byte[] ciphertext = encrypt(cipher, secret, iv, plaintext);

		return Bytes.concat(header, iv, ciphertext);
	}

	public void encrypt(ByteBuffer plaintext, ByteBuffer ciphertext) throws ShortBufferException {
		Cipher cipher = getCipher(CIPHER);

		int ivBytes = keysizeBits / 8;
		ciphertext.put(VERSION_1);

		byte[] iv = FathomdbCrypto.generateSecureRandom(ivBytes);
		ciphertext.put(iv);

		encrypt(cipher, secret, iv, plaintext, ciphertext);
	}

	@Override
	public ByteBuffer encrypt(ByteBuffer plaintext) {
		// keysize twice: once for IV, once for padding
		int capacity = plaintext.remaining() + getMaxEncryptionExtraBytes();

		ByteBuffer ciphertext = ByteBuffer.allocate(capacity);

		try {
			encrypt(plaintext, ciphertext);
		} catch (ShortBufferException e) {
			throw new IllegalStateException();
		}

		return ciphertext;
	}

	static AesCbcCryptoKey read(InputStream is) throws IOException {
		int length = is.read();
		if ((length * 8) != DEFAULT_KEYSIZE_BITS) {
			throw new IllegalArgumentException();
		}
		byte[] keyData = new byte[length];
		ByteStreams.readFully(is, keyData);

		SecretKeySpec key = new SecretKeySpec(keyData, ALGORITHM);
		return new AesCbcCryptoKey(key, DEFAULT_KEYSIZE_BITS);
	}

	@Override
	void write(OutputStream os) throws IOException {
		byte[] keyData = secret.getEncoded();
		assert keyData.length == keysizeBits / 8;
		os.write(keyData.length);
		os.write(keyData);
	}

	static AesCbcCryptoKey generateKey() {
		SecretKey secret = generateKey(ALGORITHM, DEFAULT_KEYSIZE_BITS);
		return new AesCbcCryptoKey(secret, DEFAULT_KEYSIZE_BITS);
	}

	public static AesCbcCryptoKey deriveKey(int iterationCount, byte[] salt, String password) {
		PBEKey pbeKey = KeyDerivationFunctions.doPbkdf2(iterationCount, salt, password, DEFAULT_KEYSIZE_BITS);
		SecretKey secretKey = new SecretKeySpec(pbeKey.getEncoded(), ALGORITHM);
		return new AesCbcCryptoKey(secretKey, DEFAULT_KEYSIZE_BITS);
	}

	public int getMaxEncryptionExtraBytes() {
		// keysize twice: once for IV, once for padding
		return 1 + (2 * keysizeBits / 8);
	}
}
