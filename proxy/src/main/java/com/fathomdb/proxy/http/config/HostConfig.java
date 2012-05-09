package com.fathomdb.proxy.http.config;

import com.fathomdb.config.ConfigObject;
import com.fathomdb.proxy.openstack.OpenstackCredentials;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public class HostConfig extends ConfigObject {
	public static final HostConfig NOT_PRESENT = new HostConfig(null, null,
			null);
	final String host;
	final Properties properties;

	public HostConfig(String versionKey, String host, Properties properties) {
		super(versionKey);
		this.host = host;

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

	@Override
	public boolean isPresent() {
		return properties != null;
	}

}
