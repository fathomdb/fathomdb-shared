package com.fathomdb.proxy.http.inject;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.cache.Cache;
import com.fathomdb.cache.CacheFile;
import com.fathomdb.config.ConfigurationManager;
import com.fathomdb.proxy.http.config.HttpProxyHostConfigProvider;
import com.fathomdb.proxy.http.logger.RequestLogger;
import com.google.inject.AbstractModule;

public class ProxyServerModule extends AbstractModule {
	static final Logger log = LoggerFactory.getLogger(ProxyServerModule.class);

	@Override
	protected void configure() {
		try {
			bind(ConfigurationManager.class).toInstance(ConfigurationManager.INSTANCE);

			File logDir = new File("logs");
			logDir.mkdir();
			File logFile = new File(logDir, "log" + System.currentTimeMillis() + ".log");
			RequestLogger logger = new RequestLogger(logFile);

			bind(RequestLogger.class).toInstance(logger);

			CacheFile cache = CacheFile.open(new File("cachedata000"));
			log.info("Opened cache file: " + cache);

			bind(Cache.class).toInstance(cache);

			HttpProxyHostConfigProvider configProvider = new HttpProxyHostConfigProvider(new File("hosts"));
			bind(HttpProxyHostConfigProvider.class).toInstance(configProvider);
		} catch (IOException e) {
			throw new IllegalStateException("Error initializing", e);
		}

		//
		// final Cache cache;
		// final HttpClientPool httpClientPool;
		// final OpenstackClientPool openstackClientPool;
		// final HttpProxyHostConfigProvider configProvider;
		// final OpenstackDirectoryCache openstackContainerMetadataCache;
		//
		// // TODO: This is crying out for dependency-injection
		// public RequestHandlerProvider(RequestLogger logger, HttpProxyHostConfigProvider configProvider,
		// OpenstackDirectoryCache openstackContainerMetadataCache, Cache cache, HttpClientPool httpClientPool,
		// OpenstackClientPool openstackClientPool) {
		// super();
		// this.logger = logger;
		// this.configProvider = configProvider;
		// this.openstackContainerMetadataCache = openstackContainerMetadataCache;
		// this.cache = cache;
		// this.httpClientPool = httpClientPool;
		// this.openstackClientPool = openstackClientPool;
		// OpenstackDataProvider openstack = new OpenstackDataProvider(openstackContainerMetadataCache, cache,
		// hostConfig.getOpenstackCredentials(), openstackClientPool, hostConfig.getContainerName());
		// }
	}
}
