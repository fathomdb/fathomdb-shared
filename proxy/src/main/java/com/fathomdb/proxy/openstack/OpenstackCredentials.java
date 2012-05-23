package com.fathomdb.proxy.openstack;

import java.net.URI;

import com.fathomdb.meta.Meta;

public class OpenstackCredentials {
	static final Meta<OpenstackCredentials> META = Meta.get(OpenstackCredentials.class);

	final String username;
	final String password;
	final String tenant;
	final URI authUrl;

	public OpenstackCredentials(String username, String password, String tenant, URI authUrl) {
		super();
		this.username = username;
		this.password = password;
		this.tenant = tenant;
		this.authUrl = authUrl;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getTenant() {
		return tenant;
	}

	public URI getAuthUrl() {
		return authUrl;
	}

	@Override
	public int hashCode() {
		return META.hashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return META.equals(this, obj);
	}

	@Override
	public String toString() {
		return META.toString(this);
	}

}
