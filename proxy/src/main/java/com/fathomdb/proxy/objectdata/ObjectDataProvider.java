package com.fathomdb.proxy.objectdata;

import org.jboss.netty.channel.ChannelFuture;

import com.fathomdb.proxy.http.server.GenericRequest;

public interface ObjectDataProvider {
	ChannelFuture handle(GenericRequest request, ObjectDataSink sink);
}
