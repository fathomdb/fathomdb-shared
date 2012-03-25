package com.fathomdb.proxy.http.client;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.Channels;

public class TaskWithFuture {
	protected final Channel channel;
	protected final ChannelFuture future;

	public TaskWithFuture(Channel channel) {
		this.channel = channel;
		boolean cancellable = false;
		this.future = Channels.future(channel, cancellable);
	}

	public ChannelFuture getFuture() {
		return future;
	}

	public boolean setFailure(Throwable cause) {
		return future.setFailure(cause);
	}
}
