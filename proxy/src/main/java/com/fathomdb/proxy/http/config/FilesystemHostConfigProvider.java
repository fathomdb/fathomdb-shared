package com.fathomdb.proxy.http.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class FilesystemHostConfigProvider extends HostConfigProvider {
	static final Logger log = Logger
			.getLogger(FilesystemHostConfigProvider.class);

	private final File baseDir;

	public FilesystemHostConfigProvider(File baseDir) {
		this.baseDir = baseDir;
	}

	@Override
	protected HostConfig buildHostConfig(String host) {
		safetyCheckHost(host);

		File file = new File(baseDir, host);
		if (!file.exists())
			return null;

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			Properties properties = new Properties();
			properties.load(fis);
			return new HostConfig(host, properties);
		} catch (IOException e) {
			throw new IllegalStateException("Error loading host configuration",
					e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					log.warn("Error closing file", e);
				}
			}
		}
	}

	private static void safetyCheckHost(String host) {
		for (int i = 0; i < host.length(); i++) {
			char c = host.charAt(i);
			if (c >= 'a' && c <= 'z')
				continue;
			if (c >= 'A' && c <= 'Z')
				continue;
			if (c >= '0' && c <= '9')
				continue;
			switch (c) {
			case '.':
				break;

			default:
				throw new IllegalArgumentException();
			}
		}
	}

}
