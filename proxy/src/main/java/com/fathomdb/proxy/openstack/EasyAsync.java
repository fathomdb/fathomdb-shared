package com.fathomdb.proxy.openstack;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.DefaultChannelFuture;

public abstract class EasyAsync extends DefaultChannelFuture {

	public EasyAsync(Channel channel, boolean cancellable) {
		super(channel, cancellable);
	}

	class Listener implements ChannelFutureListener {
		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (future.isSuccess()) {
				doPoll();
			} else {
				setFailure(future.getCause());
			}
		}

	};

	final Listener listener = new Listener();

	protected abstract void poll() throws Exception;

	void doPoll() {
		try {
			poll();
			setSuccess();
		} catch (AsyncFutureException e) {
			ChannelFuture future = e.getFuture();
			future.addListener(listener);
		} catch (Throwable e) {
			setFailure(e);
		}
	}
	
	public EasyAsync start() {
		doPoll();
		return this;
	}


}
