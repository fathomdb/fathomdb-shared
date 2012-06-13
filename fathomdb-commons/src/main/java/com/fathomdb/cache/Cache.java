package com.fathomdb.cache;

import org.openstack.crypto.ByteString;

import com.fathomdb.cache.CacheFile.Allocation;
import com.fathomdb.cache.CacheFile.CacheLock;

public interface Cache {

	CacheLock lookup(ByteString cacheKey);

	Allocation allocate(int contentLength);

	CacheLock store(ByteString cacheKey, Allocation allocation);

	void release(Allocation allocation);

	void writeMetadata();

}
