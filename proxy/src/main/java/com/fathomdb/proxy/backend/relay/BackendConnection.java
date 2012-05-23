package com.fathomdb.proxy.backend.relay;

import com.fathomdb.proxy.http.client.HttpClientConnectionBase;

public class BackendConnection extends HttpClientConnectionBase {
	private final BackendConnectionMap mapping;

	BackendConnection(BackendConnectionMap mapping) {
		super(mapping.pool.httpClientPool, mapping.pool.uriBase);
		this.mapping = mapping;
	}

	public BackendConnectionMap getMapping() {
		return mapping;
	}
}
