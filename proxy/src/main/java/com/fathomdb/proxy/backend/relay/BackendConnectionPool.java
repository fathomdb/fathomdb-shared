package com.fathomdb.proxy.backend.relay;

import java.net.URI;

import com.fathomdb.proxy.http.client.HttpClientPool;

public class BackendConnectionPool {
	final HttpClientPool httpClientPool;
	final URI uriBase;

	public BackendConnectionPool(HttpClientPool httpClientPool, URI uriBase) {
		this.httpClientPool = httpClientPool;
		this.uriBase = uriBase;
	}

}
