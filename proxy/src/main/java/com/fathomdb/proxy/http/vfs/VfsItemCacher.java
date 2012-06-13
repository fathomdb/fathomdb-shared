package com.fathomdb.proxy.http.vfs;

import java.io.Closeable;
import java.io.IOException;

import com.fathomdb.cache.CacheFile.CacheLock;

public interface VfsItemCacher extends Closeable {
	CacheLock doCopy() throws IOException;
}