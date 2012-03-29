package com.fathomdb.proxy.http.server;

import com.fathomdb.proxy.cache.CacheFile;
import com.fathomdb.proxy.http.client.HttpClientPool;
import com.fathomdb.proxy.http.config.HostConfig;
import com.fathomdb.proxy.http.config.HostConfigProvider;
import com.fathomdb.proxy.http.handlers.ObjectDataProviderResponseHandler;
import com.fathomdb.proxy.http.handlers.RequestHandler;
import com.fathomdb.proxy.http.logger.RequestLogger;
import com.fathomdb.proxy.openstack.OpenstackClientPool;
import com.fathomdb.proxy.openstack.OpenstackDataProvider;
import com.fathomdb.proxy.openstack.fs.OpenstackDirectoryCache;

public class RequestHandlerProvider {
	final RequestLogger logger;
	final CacheFile cache;
	final HttpClientPool httpClientPool;
	final OpenstackClientPool openstackClientPool;
	final HostConfigProvider configProvider;
	final OpenstackDirectoryCache openstackContainerMetadataCache;
	
	// TODO: This is crying out for dependency-injection
	public RequestHandlerProvider(RequestLogger logger, HostConfigProvider configProvider, OpenstackDirectoryCache openstackContainerMetadataCache, CacheFile cache,
			HttpClientPool httpClientPool,
			OpenstackClientPool openstackClientPool) {
		super();
		this.logger = logger;
		this.configProvider = configProvider;
		this.openstackContainerMetadataCache = openstackContainerMetadataCache;
		this.cache = cache;
		this.httpClientPool = httpClientPool;
		this.openstackClientPool = openstackClientPool;
	}

	public RequestHandler getRequestHandler(GenericRequest request) {
		HostConfig hostConfig = configProvider.getConfig(request);
		OpenstackDataProvider openstack = new OpenstackDataProvider(
				openstackContainerMetadataCache,
				cache,
				hostConfig.getOpenstackCredentials(), openstackClientPool,
				hostConfig.getContainerName());

		return new ObjectDataProviderResponseHandler(logger, openstack);
	}

}
