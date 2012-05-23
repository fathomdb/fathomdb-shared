package com.fathomdb.proxy.http.client;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.Channels;

public class TaskWithFuture {
	Channel channel;
	ChannelFuture future;

	public TaskWithFuture(Channel channel) {
		setChannel(channel);
	}

	public TaskWithFuture() {
	}

	public void setChannel(Channel channel) {
		if (this.channel != null) {
			throw new IllegalStateException("Channel already set");
		}

		this.channel = channel;
		boolean cancellable = false;
		this.future = Channels.future(channel, cancellable);
	}

	public ChannelFuture getFuture() {
		if (future == null) {
			throw new IllegalStateException("Channel not yet set");
		}
		return future;
	}

	public boolean setFailure(Throwable cause) {
		return getFuture().setFailure(cause);
	}

	public void setSuccess() {
		getFuture().setSuccess();
	}

	public Channel getChannel() {
		if (channel == null) {
			throw new IllegalStateException("Cannot not yet set");
		}
		return channel;
	}
}
