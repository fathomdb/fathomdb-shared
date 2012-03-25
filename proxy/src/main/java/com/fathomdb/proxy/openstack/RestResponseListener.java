package com.fathomdb.proxy.openstack;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;

import com.fathomdb.proxy.http.client.HttpResponseListener;
import com.fathomdb.proxy.http.client.TaskWithFuture;

public abstract class RestResponseListener extends TaskWithFuture implements
		HttpResponseListener {

	static final Logger log = Logger.getLogger(RestResponseListener.class);

	protected RestResponseListener(Channel channel) {
		super(channel);
	}

	StringBuilder responseBuffer;

	@Override
	public void gotData(HttpResponse response, HttpChunk chunk, boolean isLast) {
		String responseData = null;
		if (chunk == null) {
			ChannelBuffer content = response.getContent();
			if (content.readable()) {
				responseData = content.toString(CharsetUtil.UTF_8);
			}
		}

		if (responseBuffer == null) {
			responseBuffer = new StringBuilder();
		}

		if (responseData != null) {
			responseBuffer.append(responseData);
		}

		if (chunk != null) {
			ChannelBuffer content = chunk.getContent();
			if (content.readable()) {
				responseBuffer.append(content.toString(CharsetUtil.UTF_8));
			}
		}

		if (isLast) {
			try {
				log.debug("Got response: " + responseBuffer.toString());
				gotResponse(response, responseBuffer.toString());

				future.setSuccess();
			} catch (Throwable e) {
				future.setFailure(e);
			}
		}
	}

	protected abstract void gotResponse(HttpResponse response, String content)
			throws Exception;
}
