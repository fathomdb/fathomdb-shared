package com.fathomdb.proxy.openstack;

import java.net.URI;

import com.fathomdb.proxy.http.client.HttpClientConnectionBase;

public abstract class OpenstackClientBase extends HttpClientConnectionBase {
	final OpenstackClientPool openstackClientPool;

	protected OpenstackClientBase(OpenstackClientPool openstackClientPool, URI urlBase) {
		super(openstackClientPool.httpClientPool, urlBase);
		this.openstackClientPool = openstackClientPool;
	}
}
