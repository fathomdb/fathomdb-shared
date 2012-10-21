package com.fathomdb.io;

import java.io.Closeable;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoUtils {
	static final Logger log = LoggerFactory.getLogger(IoUtils.class);

	public static void safeClose(Closeable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (IOException e) {
			log.error("Ignoring unexpected error closing item", e);
		}
	}
}
