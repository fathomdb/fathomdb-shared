package com.fathomdb.proxy.openstack;

import java.net.URI;

public class SwiftClient extends OpenstackClientBase {

	public SwiftClient(OpenstackClientPool openstackClientPool, URI swiftUrl) {
		super(openstackClientPool, swiftUrl);
	}

}
