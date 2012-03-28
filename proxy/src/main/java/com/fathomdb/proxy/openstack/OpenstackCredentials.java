package com.fathomdb.proxy.openstack;

import java.net.URI;

public class OpenstackCredentials {
	final String username;
	final String password;
	final String tenant;
	final URI authUrl;

	public OpenstackCredentials(String username, String password,
			String tenant, URI authUrl) {
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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authUrl == null) ? 0 : authUrl.hashCode());
		result = prime * result
				+ ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((tenant == null) ? 0 : tenant.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OpenstackCredentials other = (OpenstackCredentials) obj;
		if (authUrl == null) {
			if (other.authUrl != null)
				return false;
		} else if (!authUrl.equals(other.authUrl))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (tenant == null) {
			if (other.tenant != null)
				return false;
		} else if (!tenant.equals(other.tenant))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

}
