package com.fathomdb;

import java.util.Random;

/**
 * Functions that generate random values; used mainly for unit tests, but used in multiple projects.
 * 
 */
public class RandomUtil {
	final Random random;

	public RandomUtil() {
		this.random = new Random();
	}

	public int uniform(int min, int max) {
		return random.nextInt(max - min) + min;
	}

	public char pick(String s) {
		return s.charAt(random.nextInt(s.length()));
	}

	public String randomAsciiString(int length) {
		char[] chars = new char[length];
		for (int i = 0; i < length; i++) {
			chars[i] = (char) uniform(32, 127);
		}
		return new String(chars);
	}

	public String randomAlphanumericString(int length) {
		char[] chars = new char[length];
		for (int i = 0; i < length; i++) {
			chars[i] = pick("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");
		}
		return new String(chars);
	}

	public String randomAlphanumericString(int minLength, int maxLength) {
		int length = uniform(minLength, maxLength);
		return randomAlphanumericString(length);
	}

	public String randomAsciiString(int minLength, int maxLength) {
		int length = uniform(minLength, maxLength);
		return randomAsciiString(length);
	}

	public String randomUnicode() {
		throw new UnsupportedOperationException();
	}

	public long nextLong() {
		return random.nextLong();
	}

	public String randomText(int minLength, int maxLength) {
		StringBuilder sb = new StringBuilder();
		int length = uniform(minLength, maxLength);

		while (sb.length() < length) {
			sb.append(randomAlphanumericString(1, 12));
			sb.append(" ");
		}

		sb.setLength(length);
		return sb.toString();
	}
}
