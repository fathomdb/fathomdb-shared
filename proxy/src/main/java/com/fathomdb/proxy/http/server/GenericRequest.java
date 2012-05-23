package com.fathomdb.proxy.http.server;

import java.util.List;
import java.util.Map.Entry;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpVersion;

public interface GenericRequest {

	boolean isKeepAlive();

	Channel getChannel();

	String getUri();

	String getHeader(String name);

	HttpMethod getMethod();

	List<Entry<String, String>> getHeaders();

	String toAbsolute(String relativePath);

	HttpVersion getProtocolVersion();

}
