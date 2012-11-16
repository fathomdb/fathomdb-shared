package com.fathomdb.io;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.Utf8;

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

	public static String readAll(Reader in) throws IOException {
		StringBuilder contents = new StringBuilder();

		char[] buffer = new char[8192];
		while (true) {
			int readCount = in.read(buffer);
			if (readCount == -1) {
				break;
			}
			contents.append(buffer, 0, readCount);
		}

		return contents.toString();
	}

	/**
	 * This closes the stream, so you don't have to worry about it
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static String readAll(InputStream inputStream) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, Utf8.CHARSET));
		try {
			return readAll(in);
		} finally {
			IoUtils.safeClose(in);
		}
	}

	public static void copyToOutputStream(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[32768];
		while (true) {
			int bytesRead = is.read(buffer);
			if (bytesRead == -1) {
				break;
			}

			os.write(buffer, 0, bytesRead);
		}
	}

}
