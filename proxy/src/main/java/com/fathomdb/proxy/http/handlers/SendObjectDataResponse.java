package com.fathomdb.proxy.http.handlers;

import java.net.SocketAddress;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.DefaultHttpChunk;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.fathomdb.proxy.http.client.TaskWithFuture;
import com.fathomdb.proxy.http.logger.RequestLogger;
import com.fathomdb.proxy.http.server.GenericRequest;
import com.fathomdb.proxy.objectdata.ObjectDataSink;

public class SendObjectDataResponse extends TaskWithFuture implements ObjectDataSink {

	private final RequestLogger logger;
	private final GenericRequest request;
	private final SocketAddress remoteAddress;

	public SendObjectDataResponse(GenericRequest request, RequestLogger logger, Channel channel) {
		super(channel);
		this.request = request;
		this.logger = logger;

		this.remoteAddress = channel.getRemoteAddress();
	}

	// There's a pain-point / catch here...
	// we don't do GZIP compression unless the initial response has content
	// So we need to hold back the response until the first bit of data comes
	int dataCount = 0;
	HttpResponse response;
	long responseLength = 0;

	@Override
	public void gotData(ChannelBuffer content, boolean isLast) {
		if (dataCount == 0) {
			sendResponse(response, content);
		} else {
			HttpChunk chunk = new DefaultHttpChunk(content);
			responseLength += chunk.getContent().readableBytes();
			getChannel().write(chunk);
		}

		dataCount++;
	}

	@Override
	public void beginResponse(HttpResponse response) {
		this.response = response;
	}

	@Override
	public void endData() {
		if (dataCount == 0) {
			sendResponse(response, null);
		}

		if (response.isChunked()) {
			HttpChunk chunk = new DefaultHttpChunk(ChannelBuffers.EMPTY_BUFFER);
			getChannel().write(chunk);
		}

		logResponse();
	}

	void logResponse() {
		logger.logResponse(remoteAddress, request, response, responseLength);
	}

	public void sendResponse(HttpResponse response, ChannelBuffer content) {
		if (!response.isChunked()) {
			if (content != null) {
				if (response.getContent() != null && response.getContent().readable()) {
					throw new IllegalStateException();
				}
				response.setContent(content);

				responseLength += content.readableBytes();
			}
		}

		getChannel().write(response);

		if (response.isChunked()) {
			if (content != null) {
				HttpChunk chunk = new DefaultHttpChunk(content);

				getChannel().write(chunk);

				responseLength += content.readableBytes();
			}
		}
	}
}
