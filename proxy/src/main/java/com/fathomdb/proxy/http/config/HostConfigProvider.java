package com.fathomdb.proxy.http.config;

import java.util.concurrent.ExecutionException;

import org.jboss.netty.handler.codec.http.HttpHeaders;

import com.fathomdb.proxy.http.server.GenericRequest;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public abstract class HostConfigProvider {

	final LoadingCache<String, HostConfig> cache = CacheBuilder.newBuilder()
			.build(new CacheLoader<String, HostConfig>() {

				@Override
				public HostConfig load(String host) throws Exception {
					return buildHostConfig(host);
				}
			});

	public HostConfig getConfig(GenericRequest request) {
		String host = request.getHeader(HttpHeaders.Names.HOST);
		if (host == null)
			throw new IllegalStateException();
		
		// Ignore port for now
		int colonIndex = host.indexOf(':');
		if (colonIndex != -1) {
			host = host.substring(0, colonIndex);
		}

		try {
			return cache.get(host);
		} catch (ExecutionException e) {
			throw new IllegalStateException("Error loading host config", e);
		}
	}

	protected abstract HostConfig buildHostConfig(String host);
}
