package com.fathomdb.proxy.openstack;

import java.net.URI;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.fathomdb.proxy.http.HttpScheme;
import com.fathomdb.proxy.http.client.HttpClientConnection;

public abstract class OpenstackClientBase implements AutoCloseable {
	final HttpScheme scheme;
	final String hostHeader;

	final OpenstackClientPool openstackClientPool;
	final URI urlBase;

	HttpClientConnection httpClient;

	public URI getBaseUrl() {
		return urlBase;
	}

	public Channel getChannel() {
		return getHttpClientConnection().getChannel();
	}

	protected OpenstackClientBase(OpenstackClientPool openstackClientPool,
			URI urlBase) {
		this.openstackClientPool = openstackClientPool;
		this.urlBase = urlBase;

		this.scheme = HttpScheme.parse(urlBase.getScheme());

		this.hostHeader = parseHostHeader(scheme, urlBase);
	}

	private static String parseHostHeader(HttpScheme scheme, URI url) {
		int port = url.getPort();
		if (port == -1) {
			port = scheme.getPort();
		}
		String hostAndPort = url.getHost() + ":" + port;
		return hostAndPort;
	}

	public HttpClientConnection getHttpClientConnection()
			throws AsyncFutureException {

		if (httpClient == null) {
			httpClient = openstackClientPool.httpClientPool.getClient(scheme,
					hostHeader);
		}

		if (!httpClient.isConnected()) {
			throw new AsyncFutureException(httpClient.connect(), "Http client connection");
		}

		return httpClient;
	}
	
	public void close() {
		if (httpClient != null) {
			httpClient.close();
		}
	}

	public HttpRequest buildRequest(HttpMethod method, String path) {
		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
				method, path);
		request.setHeader(HttpHeaders.Names.HOST, hostHeader);
		return request;
	}
	
	public <T extends OpenstackResponseHandler> T doRequest(HttpRequest request, T handler) {
		HttpClientConnection connection = getHttpClientConnection();
		handler.setChannel(connection.getChannel());
		connection.doRequest(request, handler);
		
		return handler;
	}
}
