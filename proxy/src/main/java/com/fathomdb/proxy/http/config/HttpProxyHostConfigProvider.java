package com.fathomdb.proxy.http.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jboss.netty.handler.codec.http.HttpHeaders;

import com.fathomdb.config.FilesystemConfigProvider;
import com.fathomdb.proxy.http.server.GenericRequest;

public class HttpProxyHostConfigProvider extends
		FilesystemConfigProvider<HostConfig> {

	public HttpProxyHostConfigProvider(File baseDir) {
		super(baseDir);
	}

	@Override
	protected HostConfig loadConfig(String key, String version, InputStream is)
			throws IOException {
		Properties properties = new Properties();
		properties.load(is);
		return new HostConfig(version, key, properties);
	}

	public HostConfig getConfig(GenericRequest request) {
		String host = request.getHeader(HttpHeaders.Names.HOST);
		if (host == null)
			throw new IllegalStateException();

		// Ignore port for now
		int colonIndex = host.indexOf(':');
		if (colonIndex != -1) {
			host = host.substring(0, colonIndex);
		}

		return getConfig(host);

	}

	@Override
	protected HostConfig buildNullResult(String key) {
		return HostConfig.NOT_PRESENT;
	}

}
