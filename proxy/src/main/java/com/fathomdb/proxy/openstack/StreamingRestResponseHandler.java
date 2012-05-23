package com.fathomdb.proxy.openstack;

import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

public abstract class StreamingRestResponseHandler extends OpenstackResponseHandler {

	static final Logger log = LoggerFactory.getLogger(StreamingRestResponseHandler.class);

	@Override
	public void gotData(HttpResponse response, HttpChunk chunk, boolean isLast) {
		if (chunk == null) {
			ChannelBuffer content = response.getContent();

			HttpResponseStatus httpStatus = response.getStatus();
			int httpStatusCode = httpStatus.getCode();

			switch (httpStatusCode) {
			case 200:
				break;

			default: {
				String s = null;
				if (content.readable()) {
					s = content.toString(Charsets.UTF_8);
				}
				log.info("Unexpected response code: " + httpStatusCode + ". Message=" + s);
				throw new IllegalStateException("Error authenticating.  Message=" + s);
			}
			}

			if (content.readable()) {
				try {
					gotData(content.toByteBuffer(), isLast);
				} catch (Throwable e) {
					setFailure(e);
					return;
				}
			}
		}

		if (chunk != null) {
			ChannelBuffer content = chunk.getContent();
			ByteBuffer buffer = null;
			if (content.readable()) {
				buffer = content.toByteBuffer();
			}

			try {
				gotData(buffer, isLast);
			} catch (Throwable e) {
				setFailure(e);
				return;
			}
		}

		if (isLast) {
			setSuccess();
		}
	}

	protected abstract void gotData(ByteBuffer byteBuffer, boolean isLast);

	protected abstract Object getResult();

}
