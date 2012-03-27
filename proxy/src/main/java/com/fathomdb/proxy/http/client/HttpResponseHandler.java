package com.fathomdb.proxy.http.client;

import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;

public interface HttpResponseHandler  {
	void gotData(HttpResponse response, HttpChunk chunk, boolean isLast) throws Exception;
}
