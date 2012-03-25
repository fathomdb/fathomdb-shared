package com.fathomdb.proxy.http.server;

import java.io.File;
import java.io.IOException;

import com.fathomdb.proxy.cache.CacheFile;
import com.fathomdb.proxy.cache.CachingDataProvider;
import com.fathomdb.proxy.http.client.HttpClientPool;
import com.fathomdb.proxy.http.handlers.ObjectDataProviderResponseHandler;
import com.fathomdb.proxy.http.handlers.ProxyRequestHandler;
import com.fathomdb.proxy.http.handlers.RequestHandler;
import com.fathomdb.proxy.openstack.OpenstackClientPool;
import com.fathomdb.proxy.openstack.OpenstackCredentials;
import com.fathomdb.proxy.openstack.OpenstackDataProvider;

public class RequestHandlerProvider {
	final CacheFile cache;
	final HttpClientPool httpClientPool;
	final OpenstackClientPool openstackClientPool;
	final OpenstackCredentials openstackCredentials;

	// TODO: This is crying out for dependency-injection
	public RequestHandlerProvider(CacheFile cache, HttpClientPool httpClientPool,
			OpenstackClientPool openstackClientPool,
			OpenstackCredentials openstackCredentials) {
		super();
		this.cache = cache;
		this.httpClientPool = httpClientPool;
		this.openstackClientPool = openstackClientPool;
		this.openstackCredentials = openstackCredentials;
	}

	public RequestHandler getRequestHandler(GenericRequest request) {
		OpenstackDataProvider openstack = new OpenstackDataProvider(openstackCredentials,
				openstackClientPool);

		CachingDataProvider caching = new CachingDataProvider(cache , openstack);
		return new ObjectDataProviderResponseHandler(caching);
		// return new ProxyRequestHandler(httpClientPool);

		// return new SimpleRequestHandler();
	}

}
