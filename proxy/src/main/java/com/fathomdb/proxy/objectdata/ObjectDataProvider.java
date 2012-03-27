package com.fathomdb.proxy.objectdata;

import com.fathomdb.proxy.cache.CacheFile.CacheLock;
import com.fathomdb.proxy.http.server.GenericRequest;

public abstract class ObjectDataProvider {
	public abstract Handler buildHandler(GenericRequest request);

	public abstract class Handler {
		protected final GenericRequest request;

		public Handler(GenericRequest request) {
			this.request = request;
		}

		public abstract void handle(ObjectDataSink sink);

		public boolean isStillValid(CacheLock found) {
			return false;
		}
	}
}
