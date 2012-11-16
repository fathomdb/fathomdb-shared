package com.fathomdb;

import java.nio.charset.Charset;

public class Utf8 {
	public static final Charset CHARSET = Charset.forName("UTF-8");

	public static String toString(byte[] bytes) {
		return new String(bytes, CHARSET);
	}

	public static byte[] getBytes(String s) {
		return s.getBytes(CHARSET);
	}
}
