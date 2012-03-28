package com.fathomdb.proxy.http.config;

import com.fathomdb.proxy.openstack.OpenstackCredentials;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public class HostConfig {
	final String host;
	final Properties properties;
	final String versionKey;

	public HostConfig(String host, String versionKey, Properties properties) {
		this.host = host;
		this.versionKey = versionKey;
		this.properties = properties;
	}

	OpenstackCredentials openstackCredentials;

	public OpenstackCredentials getOpenstackCredentials() {
		if (openstackCredentials == null) {
			// }
			// if (false) {
			// URI authUrl;
			// try {
			// authUrl = new URI("http://192.168.100.1:5000/v2.0/tokens");
			// } catch (URISyntaxException e) {
			// throw new IllegalArgumentException("Error parsing uri", e);
			// }
			// openstackCredentials = new OpenstackCredentials("admin", "admin",
			// "admin", authUrl);
			// } else {
			URI authUrl;
			try {
				authUrl = new URI(
						"https://identity.api.rackspacecloud.com/v2.0/tokens");
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException("Error parsing uri", e);
			}
			openstackCredentials = new OpenstackCredentials(
					properties.getProperty("openstack.user"),
					properties.getProperty("openstack.key"),
					properties.getProperty("openstack.tenant"), authUrl);
		}
		return openstackCredentials;
	}

	public String getContainerName() {
		String containerName = properties.getProperty("openstack.container");
		if (containerName == null) {
			containerName = host;
		}
		return containerName;
	}

	public String getVersionKey() {
		return versionKey;
	}

}