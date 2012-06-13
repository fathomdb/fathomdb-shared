package com.fathomdb.proxy.openstack;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fathomdb.proxy.http.client.HttpClientPool;

@Singleton
public class OpenstackClientPool {

	@Inject
	HttpClientPool httpClientPool;

	public OpenstackSession getClient(OpenstackCredentials credentials) {
		return new OpenstackSession(this, credentials);
	}

}
