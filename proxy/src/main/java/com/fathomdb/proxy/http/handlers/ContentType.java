package com.fathomdb.proxy.http.handlers;

import java.util.concurrent.ConcurrentMap;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.google.common.collect.Maps;

public class ContentType {
	final String key;

	private ContentType(String key) {
		this.key = key;
	}

	static final ConcurrentMap<String, ContentType> INSTANCES = Maps
			.newConcurrentMap();

	public static ContentType get(String key) {
		ContentType contentType = INSTANCES.get(key);

		if (contentType == null) {
			contentType = new ContentType(key);
			ContentType existing = INSTANCES.putIfAbsent(key, contentType);
			if (existing != null) {
				contentType = existing;
			}
		}

		return contentType;
	}

	@Override
	public String toString() {
		return "ContentType [" + key + "]";
	}

	public String getContentType() {
		return key;
	}

	public static ContentType get(HttpResponse response) {
		String contentType = HttpHeaders.getHeader(response,
				HttpHeaders.Names.CONTENT_TYPE);
		if (contentType == null)
			return null;
		return get(contentType);
	}
}
