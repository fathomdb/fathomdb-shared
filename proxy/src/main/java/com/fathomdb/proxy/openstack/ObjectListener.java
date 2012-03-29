package com.fathomdb.proxy.openstack;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.fathomdb.proxy.objectdata.ObjectDataSink;

public class ObjectListener extends OpenstackResponseHandler {
	static final Logger log = Logger.getLogger(ObjectListener.class);

	private final ObjectDataSink sink;
	private final HttpResponse sendResponse;

	public ObjectListener(HttpResponse sendResponse, ObjectDataSink sink) {
		this.sendResponse = sendResponse;
		this.sink = sink;
	}

	@Override
	public void gotData(HttpResponse response, HttpChunk chunk, boolean isLast)
			throws Exception {
		if (chunk == null) {
			long contentLength = HttpHeaders.getContentLength(response, -1);
			if (contentLength >= 0) {
				sendResponse.setHeader(HttpHeaders.Names.CONTENT_LENGTH, contentLength);
			}
			
			sink.beginResponse(sendResponse);
			
			ChannelBuffer content = response.getContent();
			if (content.readable()) {
				sink.gotData(content);
			}
		}

		if (chunk != null) {
			ChannelBuffer content = chunk.getContent();
			if (content.readable()) {
				sink.gotData(content);
			}
		}

		if (isLast) {
			sink.endData();
			getFuture().setSuccess();
		}
	}
}
