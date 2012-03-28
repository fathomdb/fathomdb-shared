package com.fathomdb.proxy.http.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.fathomdb.proxy.http.client.ThreadPools;
import com.google.common.base.Objects;

public class FilesystemHostConfigProvider extends HostConfigProvider {
	static final Logger log = Logger
			.getLogger(FilesystemHostConfigProvider.class);

	private final File baseDir;

	public FilesystemHostConfigProvider(File baseDir) {
		this.baseDir = baseDir;
	}

	@Override
	public void initialize() {
		ThreadPools.SYSTEM_TASK_POOL.scheduleWithFixedDelay(
				new UpdateChecker(), UpdateChecker.INTERVAL,
				UpdateChecker.INTERVAL, TimeUnit.SECONDS);
	}

	class UpdateChecker implements Runnable {
		static final int INTERVAL = 30;

		@Override
		public void run() {
			try {
				log.info("Starting filesystem refresh");

				Collection<String> keys = getKeysSnapshot();

				for (String key : keys) {
					HostConfig config = cache.getIfPresent(key);
					if (config == null) {
						log.info("Key concurrently removed: " + key);
						continue;
					}

					File file = new File(baseDir, key);
					if (!file.exists()) {
						log.info("Removed from filesystem: " + key);
						cache.refresh(key);
						continue;
					}

					String versionKey = toVersionKey(file);
					if (!Objects.equal(versionKey, config.getVersionKey())) {
						log.info("Out of date: " + key);
						cache.refresh(key);
						continue;
					}

					log.debug("Up-to-date on: " + key);
				}
			} catch (Throwable t) {
				log.warn("Error on background task", t);
			}
		}

	};

	@Override
	protected HostConfig buildHostConfig(String host) {
		safetyCheckHost(host);

		File file = new File(baseDir, host);
		if (!file.exists()) {
			// TODO: Return dummy HostConfig
			return null;
		}

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			Properties properties = new Properties();
			properties.load(fis);
			String versionKey = toVersionKey(file);
			return new HostConfig(host, versionKey, properties);
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

	public String toVersionKey(File file) {
		long lastModified = file.lastModified();
		return String.valueOf(lastModified);
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
