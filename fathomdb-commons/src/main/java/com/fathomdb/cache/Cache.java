package com.fathomdb.cache;

import com.fathomdb.cache.CacheFile.Allocation;
import com.fathomdb.cache.CacheFile.CacheLock;

public interface Cache {

	CacheLock lookup(HashKey cacheKey);

	Allocation allocate(int contentLength);

	void store(HashKey cacheKey, Allocation allocation);

}
