package com.fathomdb.proxy.http.server;

import java.util.List;
import java.util.Map.Entry;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.fathomdb.proxy.http.HttpScheme;

public class SingleChunkHttpRequest implements GenericRequest {

	final Channel channel;
	final HttpRequest request;
	private final HttpEndpoint endpoint;

	public SingleChunkHttpRequest(Channel channel, HttpEndpoint endpoint,
			HttpRequest request) {
		this.channel = channel;
		this.endpoint = endpoint;
		this.request = request;
	}

	@Override
	public boolean isKeepAlive() {
		return HttpHeaders.isKeepAlive(request);
	}

	@Override
	public Channel getChannel() {
		return channel;
	}

	@Override
	public String getUri() {
		return request.getUri();
	}

	@Override
	public String getHeader(String name) {
		return request.getHeader(name);
	}

	@Override
	public HttpMethod getMethod() {
		return request.getMethod();
	}

	@Override
	public List<Entry<String, String>> getHeaders() {
		return request.getHeaders();
	}

	@Override
	public String toAbsolute(String relativePath) {
		return endpoint.toAbsolute(request, relativePath);
	}

	@Override
	public HttpVersion getProtocolVersion() {
		return request.getProtocolVersion();
	}

}
