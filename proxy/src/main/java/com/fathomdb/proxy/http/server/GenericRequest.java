package com.fathomdb.proxy.http.server;

import java.util.List;
import java.util.Map.Entry;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpMethod;

public interface GenericRequest {

	boolean isKeepAlive();

	Channel getChannel();

	String getRequestURI();

	String getHeader(String name);

	HttpMethod getMethod();

	List<Entry<String, String>> getHeaders();

}
