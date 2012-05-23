package com.fathomdb.proxy.backend.relay;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;

import com.fathomdb.proxy.http.server.GenericRequest;
import com.fathomdb.proxy.objectdata.ObjectDataSink;
import com.fathomdb.proxy.openstack.AsyncFutureException;
import com.google.common.collect.Maps;

public class RelayRequestOperation {
	final BackendConnection connection;
	final GenericRequest request;
	final ObjectDataSink sink;

	public RelayRequestOperation(BackendConnection connection, GenericRequest request, ObjectDataSink sink) {
		this.connection = connection;
		this.request = request;
		this.sink = sink;
	}

	HttpRequest backendRequest;

	public static abstract class HeaderHandler {
		public abstract void handleHeader(HttpRequest backendRequest, String header, String headerValue);
	}

	public static class IgnoreHeader extends HeaderHandler {
		public static final IgnoreHeader INSTANCE = new IgnoreHeader();

		@Override
		public void handleHeader(HttpRequest backendRequest, String header, String headerValue) {

		}
	}

	public HttpRequest getBackendRequest() {
		if (backendRequest == null) {
			HttpMethod method = request.getMethod();
			String path = request.getUri();

			String prefix = connection.getMapping().getPrefix();
			path = Uris.removePrefix(path, prefix);

			URI backendUrl = connection.getBaseUrl();
			String backendPath = Uris.join(backendUrl.getPath(), path);

			HttpRequest backendRequest = connection.buildRequest(method, backendPath);

			Map<String, HeaderHandler> headerHandlers = Maps.newHashMap();
			headerHandlers.put("Host", IgnoreHeader.INSTANCE);
			headerHandlers.put("Connection", IgnoreHeader.INSTANCE);
			for (Entry<String, String> header : request.getHeaders()) {
				String headerName = header.getKey();
				String headerValue = header.getValue();

				HeaderHandler headerHandler = headerHandlers.get(headerName);
				if (headerHandler == null) {
					backendRequest.addHeader(headerName, headerValue);
				} else {
					headerHandler.handleHeader(backendRequest, headerName, headerValue);
				}
			}

			InetSocketAddress remoteAddress = (InetSocketAddress) request.getChannel().getRemoteAddress();
			String forwardedFor = remoteAddress.getAddress().getHostAddress();
			backendRequest.addHeader("X-Forwarded-For", forwardedFor);

			this.backendRequest = backendRequest;
		}
		return backendRequest;
	}

	BackendResponseListener httpListener;

	public void doDownload() {
		if (httpListener == null) {
			HttpRequest backendRequest = getBackendRequest();

			httpListener = connection.doRequest(backendRequest, new BackendResponseListener(sink));

			throw new AsyncFutureException(httpListener.getFuture(), "Backend get object request");
		}
	}

}
