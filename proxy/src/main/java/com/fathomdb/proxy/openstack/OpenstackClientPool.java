package com.fathomdb.proxy.openstack;

import com.fathomdb.proxy.http.client.HttpClientPool;

public class OpenstackClientPool {

	final HttpClientPool httpClientPool;

	public OpenstackClientPool(HttpClientPool httpClientPool) {
		this.httpClientPool = httpClientPool;
	}

	public OpenstackSession getClient(OpenstackCredentials credentials) {
		return new OpenstackSession(this, credentials);
	}

}
