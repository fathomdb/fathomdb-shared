package com.fathomdb.proxy.http.handlers;

import org.jboss.netty.channel.ChannelFuture;

import com.fathomdb.proxy.http.server.GenericRequest;

public interface RequestHandler {

	ChannelFuture handleRequest(GenericRequest request);

}
