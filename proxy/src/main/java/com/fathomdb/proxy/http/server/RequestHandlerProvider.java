package com.fathomdb.proxy.http.server;

import com.fathomdb.proxy.cache.CacheFile;
import com.fathomdb.proxy.http.client.HttpClientPool;
import com.fathomdb.proxy.http.config.HostConfig;
import com.fathomdb.proxy.http.config.HostConfigProvider;
import com.fathomdb.proxy.http.handlers.ObjectDataProviderResponseHandler;
import com.fathomdb.proxy.http.handlers.RequestHandler;
import com.fathomdb.proxy.openstack.OpenstackClientPool;
import com.fathomdb.proxy.openstack.OpenstackCredentials;
import com.fathomdb.proxy.openstack.OpenstackDataProvider;

public class RequestHandlerProvider {
	final CacheFile cache;
	final HttpClientPool httpClientPool;
	final OpenstackClientPool openstackClientPool;
	final HostConfigProvider configProvider;
	
	// TODO: This is crying out for dependency-injection
	public RequestHandlerProvider(HostConfigProvider configProvider, CacheFile cache,
			HttpClientPool httpClientPool,
			OpenstackClientPool openstackClientPool) {
		super();
		this.configProvider = configProvider;
		this.cache = cache;
		this.httpClientPool = httpClientPool;
		this.openstackClientPool = openstackClientPool;
	}

	public RequestHandler getRequestHandler(GenericRequest request) {
		HostConfig hostConfig = configProvider.getConfig(request);
		OpenstackDataProvider openstack = new OpenstackDataProvider(cache,
				hostConfig.getOpenstackCredentials(), openstackClientPool,
				hostConfig.getContainerName());

		return new ObjectDataProviderResponseHandler(openstack);
	}

}
