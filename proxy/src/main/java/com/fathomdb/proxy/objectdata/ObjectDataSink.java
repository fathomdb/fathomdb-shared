package com.fathomdb.proxy.objectdata;

import org.jboss.netty.buffer.ChannelBuffer;

public interface ObjectDataSink {

	void gotData(ChannelBuffer content);

	void beginData(long contentLength);

	void endData();

}
