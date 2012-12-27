package com.fathomdb.io;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.Utf8;
import com.google.common.base.Charsets;
import com.google.common.io.Closeables;

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

	public static void safeDelete(File file) {
		if (file == null) {
			return;
		}

		if (!file.delete()) {
			log.error("Could not delete file: " + file);
		}
	}

	public static void checkedDelete(File file) throws IOException {
		if (file == null) {
			return;
		}

		if (!file.delete()) {
			throw new IOException("Could not delete file: " + file);
		}
	}

	public static long copyStream(InputStream input, OutputStream output) throws IOException {
		long bytesCopied = 0;
		byte[] buffer = new byte[32768];
		while (true) {
			int bytesRead = input.read(buffer);
			if (bytesRead <= 0) {
				break;
			}
			output.write(buffer, 0, bytesRead);
			bytesCopied += bytesRead;
		}
		return bytesCopied;
	}

	public static void writeAll(File file, String contents) throws IOException {
		Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);
		try {
			writer.write(contents);
			writer.flush();
		} finally {
			writer.close();
		}
	}

	public static void writeAll(File file, InputStream is) throws IOException {
		FileOutputStream os = new FileOutputStream(file);
		try {
			copyStream(is, os);
		} finally {
			os.close();
		}
	}

	public static void copyStream(InputStream is, File outputFile) throws IOException {
		FileOutputStream fos = new FileOutputStream(outputFile);
		try {
			copyStream(is, fos);
		} finally {
			Closeables.closeQuietly(fos);
		}
	}

	public static String readAll(URL url) throws IOException {
		InputStream is = url.openStream();
		try {
			return readAll(is);
		} finally {
			safeClose(is);
		}
	}

	public static File resolve(String filename) {
		if (filename.startsWith("~/")) {
			filename = filename.replace("~/", System.getProperty("user.home") + File.separator);
		}

		return new File(filename);
	}

}
