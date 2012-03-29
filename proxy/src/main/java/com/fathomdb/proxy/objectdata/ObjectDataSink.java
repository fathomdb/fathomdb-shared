package com.fathomdb.proxy.objectdata;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpResponse;

public interface ObjectDataSink {

	void gotData(ChannelBuffer content, boolean isLast);

	void beginResponse(HttpResponse response);

	void endData();

}
