package com.fathomdb.proxy.http.server;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;

import com.fathomdb.proxy.http.HttpScheme;

public class HttpEndpoint {

	private final HttpScheme scheme;

	public HttpEndpoint(HttpScheme scheme) {
		this.scheme = scheme;
	}

	public String toAbsolute(HttpRequest request, String relativePath) {
		String hostAndPort = request.getHeader(HttpHeaders.Names.HOST);
		String uri = scheme.toString().toLowerCase() + "://" + hostAndPort
				+ relativePath;
		return uri;
	}

}
