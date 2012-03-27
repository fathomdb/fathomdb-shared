package com.fathomdb.proxy.openstack.fs;

import java.util.Map;

import com.google.common.collect.Maps;

public class OpenstackDirectoryCache {

	public static final OpenstackDirectoryCache INSTANCE = new OpenstackDirectoryCache();

	final Map<String, CacheEntry> cache = Maps.newHashMap();

	public static class CacheEntry {
		public final OpenstackItem root;

		public CacheEntry(OpenstackItem root) {
			this.root = root;
		}
	}

	public CacheEntry find(String key) {
		return cache.get(key);
	}

	public void put(String key, CacheEntry entry) {
		cache.put(key, entry);
	}
}
