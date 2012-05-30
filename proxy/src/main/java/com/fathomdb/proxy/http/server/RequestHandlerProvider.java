package com.fathomdb.proxy.http.server;

import java.net.URI;

import com.fathomdb.cache.Cache;
import com.fathomdb.proxy.backend.relay.BackendConnectionMap;
import com.fathomdb.proxy.backend.relay.BackendConnectionPool;
import com.fathomdb.proxy.backend.relay.RelayObjectDataProvider;
import com.fathomdb.proxy.http.client.HttpClientPool;
import com.fathomdb.proxy.http.config.HostConfig;
import com.fathomdb.proxy.http.config.HttpProxyHostConfigProvider;
import com.fathomdb.proxy.http.handlers.ObjectDataProviderResponseHandler;
import com.fathomdb.proxy.http.handlers.RequestHandler;
import com.fathomdb.proxy.http.logger.RequestLogger;
import com.fathomdb.proxy.openstack.OpenstackClientPool;
import com.fathomdb.proxy.openstack.OpenstackDataProvider;
import com.fathomdb.proxy.openstack.fs.OpenstackDirectoryCache;

public class RequestHandlerProvider {
	final RequestLogger logger;
	final Cache cache;
	final HttpClientPool httpClientPool;
	final OpenstackClientPool openstackClientPool;
	final HttpProxyHostConfigProvider configProvider;
	final OpenstackDirectoryCache openstackContainerMetadataCache;

	// TODO: This is crying out for dependency-injection
	public RequestHandlerProvider(RequestLogger logger, HttpProxyHostConfigProvider configProvider,
			OpenstackDirectoryCache openstackContainerMetadataCache, Cache cache, HttpClientPool httpClientPool,
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
		String uri = request.getUri();

		HostConfig hostConfig = configProvider.getConfig(request);

		if (uri.startsWith("/relay/")) {
			URI uriBase = URI.create("http://127.0.0.1:8888/");
			BackendConnectionPool backendConnectionPool = new BackendConnectionPool(httpClientPool, uriBase);

			BackendConnectionMap map = new BackendConnectionMap("/relay/", backendConnectionPool);
			RelayObjectDataProvider provider = new RelayObjectDataProvider(map);

			return new ObjectDataProviderResponseHandler(logger, provider);
		}

		OpenstackDataProvider openstack = new OpenstackDataProvider(openstackContainerMetadataCache, cache,
				hostConfig.getOpenstackCredentials(), openstackClientPool, hostConfig.getContainerName());

		return new ObjectDataProviderResponseHandler(logger, openstack);
	}

}
