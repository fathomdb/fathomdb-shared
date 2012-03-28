package com.fathomdb.proxy.http.config;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

import org.jboss.netty.handler.codec.http.HttpHeaders;

import com.fathomdb.proxy.http.server.GenericRequest;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

public abstract class HostConfigProvider {

	protected final LoadingCache<String, HostConfig> cache = CacheBuilder
			.newBuilder().build(new CacheLoader<String, HostConfig>() {

				@Override
				public HostConfig load(String host) throws Exception {
					return buildHostConfig(host);
				}
			});

	// TODO: Call automatically?
	public void initialize() {

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

		try {
			return cache.get(host);
		} catch (ExecutionException e) {
			throw new IllegalStateException("Error loading host config", e);
		}
	}

	/**
	 * Do our best to get a snapshot of the keys in the cache
	 */
	protected Collection<String> getKeysSnapshot() {
		return Lists.newArrayList(cache.asMap().keySet());
	}

	protected abstract HostConfig buildHostConfig(String host);
}
