package com.fathomdb.proxy.cache;

import java.nio.ByteBuffer;
import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.HttpMethod;

import com.fathomdb.proxy.cache.CacheFile.CacheLock;
import com.fathomdb.proxy.http.server.GenericRequest;
import com.fathomdb.proxy.objectdata.ObjectDataProvider;
import com.fathomdb.proxy.objectdata.ObjectDataSink;

public class CachingDataProvider extends ObjectDataProvider {
	static final Logger log = Logger.getLogger(CachingDataProvider.class);

	final CacheFile cache;
	final ObjectDataProvider missHandler;

	public CachingDataProvider(CacheFile cache, ObjectDataProvider missHandler) {
		this.cache = cache;
		this.missHandler = missHandler;
	}

	@Override
	public ChannelFuture handle(GenericRequest request, ObjectDataSink sink) {
		final String requestURI = request.getRequestURI();
		String hostAndPort = request.getHeader("Host");
		HttpMethod method = request.getMethod();

		HashKey cacheKey = null;

		if (method.equals(HttpMethod.GET)) {
			// TODO: Make this more robust
			String keyString = hostAndPort + ":" + requestURI;
			cacheKey = new HashKey(keyString.getBytes());
		}

		if (cacheKey != null) {
			CacheLock found = cache.lookup(cacheKey);
			try {
				if (found != null) {
					ByteBuffer buffer = found.buffer;

					if (missHandler.isStillValid(found)) {
						sink.beginData(buffer.remaining());
						sink.gotData(ChannelBuffers.wrappedBuffer(buffer));
						sink.endData();
						return null;
					}
				}
			} finally {
				if (found != null)
					found.close();
			}
		}

		if (cacheKey == null) {
			// Can't cache
			return missHandler.handle(request, sink);
		} else {
			return missHandler.handle(request, new ObjectDataSinkSplitter(
					new CachingObjectDataSink(cache, cacheKey), sink));
		}
	}

}
