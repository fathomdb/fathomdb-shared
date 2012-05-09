package com.fathomdb.config;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

public abstract class ConfigProvider<T> implements HasConfiguration {

	protected final LoadingCache<String, T> cache = CacheBuilder.newBuilder()
			.build(new CacheLoader<String, T>() {
				@Override
				public T load(String host) throws Exception {
					return buildConfig(host);
				}
			});

	// TODO: Call automatically?
	public void initialize() {

	}

	public T getConfig(String key) {
		if (key == null)
			throw new IllegalStateException();

		try {
			return cache.get(key);
		} catch (ExecutionException e) {
			throw new IllegalStateException("Error loading config", e);
		}
	}

	/**
	 * Do our best to get a snapshot of the keys in the cache
	 */
	protected Collection<String> getKeysSnapshot() {
		return Lists.newArrayList(cache.asMap().keySet());
	}

	protected abstract T buildConfig(String key);
}
