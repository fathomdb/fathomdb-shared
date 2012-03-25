package com.fathomdb.proxy.http.handlers;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.DefaultHttpChunk;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import com.fathomdb.proxy.http.client.TaskWithFuture;
import com.fathomdb.proxy.objectdata.ObjectDataSink;

public class SendObjectDataResponse extends TaskWithFuture implements
		ObjectDataSink {

	public SendObjectDataResponse(Channel channel) {
		super(channel);
	}

	@Override
	public void gotData(ChannelBuffer content) {
		HttpChunk chunk = new DefaultHttpChunk(content);
		channel.write(chunk);
	}

	@Override
	public void beginData(long contentLength) {
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
		if (contentLength < 0) {
			throw new UnsupportedOperationException();
		}

		if (contentLength != 0) {
			response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, contentLength);
		}

		channel.write(response);
	}

	@Override
	public void endData() {

	}

}
