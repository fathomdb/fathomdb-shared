package com.fathomdb.proxy.http.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.openstack.crypto.ByteString;

import com.fathomdb.config.ConfigObject;
import com.fathomdb.proxy.objectdata.ObjectDataProvider;
import com.fathomdb.proxy.openstack.OpenstackCredentials;
import com.fathomdb.proxy.openstack.OpenstackDataProvider;
import com.fathomdb.utils.Hex;
import com.google.inject.Injector;

public class HostConfig extends ConfigObject {
	public static final HostConfig NOT_PRESENT = new HostConfig(null, null, null, null);
	final String host;
	final Properties properties;
	final Injector injector;

	public HostConfig(Injector injector, String versionKey, String host, Properties properties) {
		super(versionKey);
		this.injector = injector;
		this.host = host;

		this.properties = properties;
	}

	OpenstackCredentials openstackCredentials;

	public OpenstackCredentials getOpenstackCredentials() {
		if (openstackCredentials == null) {
			String openstackUrl = properties.getProperty("openstack.url");
			if (openstackUrl == null) {
				openstackUrl = "https://identity.api.rackspacecloud.com/v2.0/tokens";
			}

			URI authUrl;
			try {
				authUrl = new URI(openstackUrl);
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException("Error parsing uri", e);
			}

			openstackCredentials = new OpenstackCredentials(properties.getProperty("openstack.user"),
					properties.getProperty("openstack.key"), properties.getProperty("openstack.tenant"), authUrl);
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

	public String getProviderClassName() {
		String providerClass = properties.getProperty("provider");
		if (providerClass == null) {
			providerClass = OpenstackDataProvider.class.getName();
		}
		return providerClass;
	}

	Class<?> providerClassFactory;

	public Class<?> getProviderClassFactory() {
		if (providerClassFactory == null) {
			String className = getProviderClassName();
			Class<?> clazz;
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException("Error loading provider class: " + className, e);
			}
			providerClassFactory = clazz;
		}
		return providerClassFactory;
	}

	@Override
	public boolean isPresent() {
		return properties != null;
	}

	ObjectDataProvider dataProvider;

	public ObjectDataProvider getDataProvider() {
		if (dataProvider == null) {
			dataProvider = (ObjectDataProvider) injector.getInstance(getProviderClassFactory());
			dataProvider.initialize(this);
		}
		return dataProvider;
	}

	public ByteString getRootDirectoryCasKey() {
		String key = properties.getProperty("root.key");
		if (key == null) {
			throw new IllegalStateException("root.key property not found");
		}
		return new ByteString(Hex.fromHex(key));
	}
}
