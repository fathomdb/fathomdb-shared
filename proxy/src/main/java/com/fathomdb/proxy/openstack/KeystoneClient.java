package com.fathomdb.proxy.openstack;

import java.net.URI;

public class KeystoneClient extends OpenstackClientBase {

	protected KeystoneClient(OpenstackClientPool openstackClientPool,
			URI urlBase) {
		super(openstackClientPool, urlBase);
	}

}
