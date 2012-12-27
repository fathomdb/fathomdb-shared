package com.fathomdb;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class Utf8 {
	public static final Charset CHARSET = Charset.forName("UTF-8");

	public static byte[] getBytes(String s) {
		return s.getBytes(CHARSET);
	}

	public static InputStreamReader openFile(File file) throws FileNotFoundException {
		return new InputStreamReader(new FileInputStream(file), CHARSET);
	}

	public static String toString(ByteBuffer buffer) {
		CharsetDecoder decoder = CHARSET.newDecoder();
		CharBuffer chars;
		try {
			chars = decoder.decode(buffer);
		} catch (CharacterCodingException e) {
			throw new IllegalArgumentException("Error convert from UTF8", e);
		}
		return chars.toString();
	}

	public static String toString(byte[] bytes) {
		return new String(bytes, CHARSET);
	}

	public static String toString(ByteArrayOutputStream baos) {
		return toString(baos.toByteArray());
	}
}
