package com.fathomdb.proxy.openstack;

import java.net.URI;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.fathomdb.async.AsyncFutureException;
import com.fathomdb.proxy.objectdata.ObjectDataSink;

public class DownloadObjectOperation {
	final OpenstackSession session;
	final String path;
	final ObjectDataSink sink;
	final HttpResponse sendResponse;

	public DownloadObjectOperation(OpenstackSession session, String path, HttpResponse sendResponse, ObjectDataSink sink) {
		this.session = session;
		this.path = path;
		this.sendResponse = sendResponse;
		this.sink = sink;
	}

	ObjectListener httpListener;

	public void doDownload() {
		if (httpListener == null) {
			SwiftClient swift = session.getSwiftClient();
			URI swiftUrl = swift.getBaseUrl();

			String fullPath = swiftUrl.getPath();
			fullPath += "/" + path;

			HttpRequest request = swift.buildRequest(HttpMethod.GET, fullPath);
			request.setHeader("X-Auth-Token", session.getAuthTokenId());
			request.setHeader(HttpHeaders.Names.USER_AGENT, "Java Proxy");

			// TODO: Use keep-alive
			// request.setHeader(HttpHeaders.Names.CONNECTION,
			// HttpHeaders.Values.KEEP_ALIVE);

			httpListener = swift.doRequest(request, new ObjectListener(sendResponse, sink));

			throw new AsyncFutureException(httpListener.getFuture(), "Swift get object request");
		}

	}
}
