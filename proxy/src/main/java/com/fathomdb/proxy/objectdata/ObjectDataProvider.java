package com.fathomdb.proxy.objectdata;

import org.jboss.netty.channel.ChannelFuture;

import com.fathomdb.proxy.cache.CacheFile.CacheLock;
import com.fathomdb.proxy.http.server.GenericRequest;

public abstract class ObjectDataProvider {
	public abstract ChannelFuture handle(GenericRequest request,
			ObjectDataSink sink);

	public boolean isStillValid(CacheLock found) {
		return false;
	}
}
