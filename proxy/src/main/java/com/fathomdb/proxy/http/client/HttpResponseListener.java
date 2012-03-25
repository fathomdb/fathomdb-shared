package com.fathomdb.proxy.http.client;

import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;

public interface HttpResponseListener {
	void gotData(HttpResponse response, HttpChunk chunk, boolean isLast) throws Exception;
}
