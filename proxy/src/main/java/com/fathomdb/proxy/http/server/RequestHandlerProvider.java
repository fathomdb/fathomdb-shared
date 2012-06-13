package com.fathomdb.proxy.http.server;

import javax.inject.Inject;

import com.fathomdb.proxy.http.config.HostConfig;
import com.fathomdb.proxy.http.config.HttpProxyHostConfigProvider;
import com.fathomdb.proxy.http.handlers.ObjectDataProviderResponseHandler;
import com.fathomdb.proxy.http.handlers.RequestHandler;
import com.fathomdb.proxy.http.logger.RequestLogger;
import com.fathomdb.proxy.objectdata.ObjectDataProvider;

public class RequestHandlerProvider {
	@Inject
	RequestLogger logger;

	@Inject
	HttpProxyHostConfigProvider configProvider;

	public RequestHandler getRequestHandler(GenericRequest request) {
		String uri = request.getUri();

		HostConfig hostConfig = configProvider.getConfig(request);

		// if (uri.startsWith("/relay/")) {
		// URI uriBase = URI.create("http://127.0.0.1:8888/");
		// BackendConnectionPool backendConnectionPool = new BackendConnectionPool(httpClientPool, uriBase);
		//
		// BackendConnectionMap map = new BackendConnectionMap("/relay/", backendConnectionPool);
		// RelayObjectDataProvider provider = new RelayObjectDataProvider(map);
		//
		// return new ObjectDataProviderResponseHandler(logger, provider);
		// }

		ObjectDataProvider objectDataProvider = hostConfig.getDataProvider();

		return new ObjectDataProviderResponseHandler(logger, objectDataProvider);
	}

}
