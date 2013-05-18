package com.fathomdb.crypto;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public abstract class CryptoKey {
	// @SuppressWarnings("unused")
	// private static final Logger log = LoggerFactory.getLogger(CryptoKey.class);

	public abstract byte[] decrypt(byte[] ciphertext);

	public abstract byte[] encrypt(byte[] plaintext);
    public abstract ByteBuffer encrypt(ByteBuffer plaintext);

    protected static Cipher getCipher(String algorithm) {
		try {
			return Cipher.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("Error loading crypto provider", e);
		} catch (NoSuchPaddingException e) {
			throw new IllegalArgumentException("Error loading crypto provider", e);
		}
	}

	protected static byte[] decrypt(Cipher cipher, Key key, byte[] iv, byte[] cipherText) {
		initDecrypt(cipher, iv, key);
		byte[] plainText;
		try {
			plainText = cipher.doFinal(cipherText);
		} catch (IllegalBlockSizeException e) {
			throw new IllegalArgumentException("Error in decryption", e);
		} catch (BadPaddingException e) {
			throw new IllegalArgumentException("Error in decryption", e);
		}
		return plainText;
	}

	protected static void initDecrypt(Cipher cipher, byte[] iv, Key key) {
		try {
			if (iv != null) {
				cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
			} else {
				cipher.init(Cipher.DECRYPT_MODE, key);
			}
		} catch (InvalidKeyException e) {
			throw new IllegalArgumentException("Invalid key", e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new IllegalArgumentException("Invalid key", e);
		}
	}

	protected static void initEncrypt(Cipher cipher, byte[] iv, Key key) {
		try {
			if (iv != null) {
				cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
			} else {
				cipher.init(Cipher.ENCRYPT_MODE, key);
			}
		} catch (InvalidKeyException e) {
			throw new IllegalArgumentException("Invalid key", e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new IllegalArgumentException("Invalid key", e);
		}
	}

	protected static byte[] encrypt(Cipher cipher, Key key, byte[] iv, byte[] plaintext) {
		initEncrypt(cipher, iv, key);
		byte[] encryptedBytes;
		try {
            encryptedBytes = cipher.doFinal(plaintext);
		} catch (IllegalBlockSizeException e) {
			throw new IllegalArgumentException("Error in encryption", e);
		} catch (BadPaddingException e) {
			throw new IllegalArgumentException("Error in encryption", e);
		}
		return encryptedBytes;
	}

    protected static int encrypt(Cipher cipher, Key key, byte[] iv, ByteBuffer plaintext, ByteBuffer ciphertext) throws ShortBufferException {
        initEncrypt(cipher, iv, key);
        try {
            return cipher.doFinal(plaintext, ciphertext);
        } catch (IllegalBlockSizeException e) {
            throw new IllegalArgumentException("Error in encryption", e);
        } catch (BadPaddingException e) {
            throw new IllegalArgumentException("Error in encryption", e);
        }
    }

    protected static SecretKey generateKey(String algorithm, int keysize) {
		KeyGenerator generator;
		try {
			generator = KeyGenerator.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("Error loading crypto provider", e);
		}
		generator.init(keysize);
		SecretKey key = generator.generateKey();
		return key;
	}

	abstract void write(OutputStream os) throws IOException;
}
