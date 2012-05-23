package com.fathomdb.proxy.backend.relay;

public class Uris {

	public static String join(String base, String relative) {
		if (base.endsWith("/")) {
			if (relative.startsWith("/")) {
				return base + relative.substring(1);
			} else {
				return base + relative;
			}
		} else {
			if (relative.startsWith("/")) {
				return base + relative;
			} else {
				return base + "/" + relative;
			}
		}
	}

	public static String removePrefix(String path, String prefix) {
		if (!path.startsWith(prefix)) {
			throw new IllegalStateException();
		}

		return path.substring(prefix.length());
	}
}
