package com.fathomdb.proxy.openstack;

import com.fathomdb.proxy.http.client.HttpClientPool;

public class OpenstackClientPool {

	final HttpClientPool httpClientPool;

	public OpenstackClientPool(HttpClientPool httpClientPool) {
		this.httpClientPool = httpClientPool;
	}

	public OpenstackClient getClient(OpenstackCredentials credentials) {
		return new OpenstackClient(this, credentials);
	}

}
