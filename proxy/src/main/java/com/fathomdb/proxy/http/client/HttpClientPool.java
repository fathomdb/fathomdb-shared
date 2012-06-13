package com.fathomdb.proxy.http.client;

import java.net.URI;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fathomdb.proxy.http.HttpScheme;

@Singleton
public class HttpClientPool {
	@Inject
	HttpClient client;

	public HttpClientConnection getClient(HttpScheme scheme, String hostAndPort) {
		// TODO: Implement pooling!!

		String host = hostAndPort;
		int port;
		int colonIndex = host.indexOf(':');
		if (colonIndex == -1) {
			port = scheme.getPort();
		} else {
			String portString = hostAndPort.substring(colonIndex + 1);
			host = host.substring(0, colonIndex);

			port = Integer.parseInt(portString);
		}
		HttpClientConnection connection = new HttpClientConnection(client, scheme, host, port);
		return connection;
	}

	public HttpClientConnection getClient(URI url) {
		HttpScheme scheme = HttpScheme.parse(url.getScheme());
		int port = url.getPort();
		if (port == -1) {
			port = scheme.getPort();
		}
		String hostAndPort = url.getHost() + ":" + port;
		return getClient(scheme, hostAndPort);
	}
}
