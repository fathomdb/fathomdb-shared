package com.fathomdb.proxy.openstack;

import org.jboss.netty.channel.ChannelFuture;

public class AsyncFutureException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	final ChannelFuture future;

	public ChannelFuture getFuture() {
		return future;
	}

	public AsyncFutureException(ChannelFuture future) {
		super();
		this.future = future;
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

}
