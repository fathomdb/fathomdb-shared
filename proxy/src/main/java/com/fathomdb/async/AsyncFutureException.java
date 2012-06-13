package com.fathomdb.async;

import org.jboss.netty.channel.ChannelFuture;

public class AsyncFutureException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	final ChannelFuture future;

	public ChannelFuture getFuture() {
		return future;
	}

	public AsyncFutureException(ChannelFuture future, String message) {
		super(message);
		this.future = future;
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		// For performance, don't fill in stack-trace
		return this;
	}

}
