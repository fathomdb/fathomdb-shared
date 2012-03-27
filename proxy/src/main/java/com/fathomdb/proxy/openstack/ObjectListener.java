package com.fathomdb.proxy.openstack;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.fathomdb.proxy.objectdata.ObjectDataSink;

public class ObjectListener extends OpenstackResponseHandler {

	private final ObjectDataSink sink;

	public ObjectListener(ObjectDataSink sink) {
		this.sink = sink;
	}

	@Override
	public void gotData(HttpResponse response, HttpChunk chunk, boolean isLast)
			throws Exception {
		if (chunk == null) {
			// TODO: Send header??
			long contentLength = HttpHeaders.getContentLength(response, -1);
			sink.beginData(contentLength);

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
