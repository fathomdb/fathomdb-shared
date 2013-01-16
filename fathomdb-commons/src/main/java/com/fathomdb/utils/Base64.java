package com.fathomdb.utils;

import java.io.IOException;

public class Base64 {

	public static String encode(byte[] data) {
		String base64 = com.fathomdb.repackaged.net.iharder.NetIHarderBase64.encodeBytes(data);
		// new sun.misc.BASE64Encoder().encode(data);
		while (base64.endsWith("\r") || base64.endsWith("\n")) {
			base64 = base64.substring(0, base64.length() - 1);
		}
		return base64;
	}

	public static byte[] decode(String base64) {
		try {
			return com.fathomdb.repackaged.net.iharder.NetIHarderBase64.decode(base64);
			// return new sun.misc.BASE64Decoder().decodeBuffer(base64);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error decoding base-64 encoded string", e);
		}
	}

}
