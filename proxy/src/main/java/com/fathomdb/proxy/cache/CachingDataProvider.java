package com.fathomdb.proxy.cache;

import java.nio.ByteBuffer;
import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpMethod;

import com.fathomdb.proxy.cache.CacheFile.CacheLock;
import com.fathomdb.proxy.http.server.GenericRequest;
import com.fathomdb.proxy.objectdata.ObjectDataProvider;
import com.fathomdb.proxy.objectdata.ObjectDataSink;

public class CachingDataProvider extends ObjectDataProvider {
	static final Logger log = Logger.getLogger(CachingDataProvider.class);

	final CacheFile cache;
	final ObjectDataProvider missHandlerProvider;

	public CachingDataProvider(CacheFile cache,
			ObjectDataProvider missHandlerProvider) {
		this.cache = cache;
		this.missHandlerProvider = missHandlerProvider;
	}

	class Handler extends ObjectDataProvider.Handler {
		public Handler(GenericRequest request) {
			super(request);
		}

		ObjectDataProvider.Handler missHandler;

		ObjectDataProvider.Handler getMissHandler() {
			if (missHandler == null) {
				missHandler = missHandlerProvider.buildHandler(request);
			}
			return missHandler;
		}

		@Override
		public void handle(ObjectDataSink sink) {
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

						if (getMissHandler().isStillValid(found)) {
							log.info("Cache HIT on " + cacheKey);
							
							sink.beginData(buffer.remaining());
							sink.gotData(ChannelBuffers.wrappedBuffer(buffer));
							sink.endData();
							return;
						}
						else {
							log.info("Cache OUTOFDATE on " + cacheKey + " (hit but no longer valid)");
						}
					} else {
						log.info("Cache MISS on " + cacheKey);
					}
				} finally {
					if (found != null)
						found.close();
				}
			}

			if (cacheKey == null) {
				// Can't cache
				getMissHandler().handle(sink);
			} else {
				getMissHandler().handle(
						new ObjectDataSinkSplitter(new CachingObjectDataSink(
								cache, cacheKey), sink));
			}
		}

	}

	@Override
	public Handler buildHandler(GenericRequest request) {
		return new Handler(request);
	}

}
