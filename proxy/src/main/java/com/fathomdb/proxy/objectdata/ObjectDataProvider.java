package com.fathomdb.proxy.objectdata;

import com.fathomdb.proxy.http.config.HostConfig;
import com.fathomdb.proxy.http.server.GenericRequest;

public abstract class ObjectDataProvider {
	public abstract Handler buildHandler(GenericRequest request);

	public abstract class Handler implements AutoCloseable {
		protected final GenericRequest request;

		public Handler(GenericRequest request) {
			this.request = request;
		}

		public abstract void handle(ObjectDataSink sink) throws Exception;
	}

	public void initialize(HostConfig hostConfig) {
	}
}
