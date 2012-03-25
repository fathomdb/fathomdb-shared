package com.fathomdb.proxy.http;

public enum HttpScheme {
	HTTP(80), HTTPS(443);

	final int port;

	private HttpScheme(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public static HttpScheme parse(String scheme) {
		int length = scheme.length();
		switch (length) {
		case 4:
			if (scheme.equalsIgnoreCase("http")) {
				return HTTP;
			}
			break;
		case 5:
			if (scheme.equalsIgnoreCase("https")) {
				return HTTPS;
			}
			break;
		}

		throw new IllegalArgumentException("Unknown scheme: " + scheme);
	}

}
