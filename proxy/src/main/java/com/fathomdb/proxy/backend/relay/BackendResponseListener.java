package com.fathomdb.proxy.backend.relay;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.proxy.http.client.HttpResponseHandler;
import com.fathomdb.proxy.http.client.TaskWithFuture;
import com.fathomdb.proxy.objectdata.ObjectDataSink;

public class BackendResponseListener extends TaskWithFuture implements HttpResponseHandler {
	static final Logger log = LoggerFactory.getLogger(BackendResponseListener.class);

	private final ObjectDataSink sink;

	public BackendResponseListener(ObjectDataSink sink) {
		this.sink = sink;
	}

	@Override
	public void gotData(HttpResponse response, HttpChunk chunk, boolean isLast) throws Exception {
		if (chunk == null) {
			// long contentLength = HttpHeaders.getContentLength(response, -1);
			// if (contentLength >= 0) {
			// log.warn("Setting content length to " + contentLength);
			// sendResponse.setHeader(HttpHeaders.Names.CONTENT_LENGTH, contentLength);
			// } else {
			// log.warn("Not setting content length");
			// }

			boolean headerIsLast = isLast && (chunk == null || !chunk.getContent().readable());
			if (!headerIsLast) {
				response.setChunked(true);
			}

			ChannelBuffer content = response.getContent();
			response.setContent(null);

			sink.beginResponse(response);

			if (content.readable()) {
				sink.gotData(content, headerIsLast);
			}
		}

		if (chunk != null) {
			ChannelBuffer content = chunk.getContent();
			if (content.readable()) {
				sink.gotData(content, isLast);
			}
		}

		if (isLast) {
			sink.endData();
			getFuture().setSuccess();
		}

	}
}
