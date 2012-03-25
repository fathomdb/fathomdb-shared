package com.fathomdb.proxy.openstack;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.DefaultChannelFuture;

public class Futures {
	static class Task {
		Callable<ChannelFuture> callable;
		ChannelFuture future;
	};

	public static class FutureChain extends DefaultChannelFuture {
		Queue<Task> taskList = new LinkedList<Task>();

		private FutureChain(Channel channel, boolean cancellable) {
			super(channel, cancellable);
		}

		class Listener implements ChannelFutureListener {

			@Override
			public void operationComplete(ChannelFuture future)
					throws Exception {
				if (future.isSuccess()) {
					poll();
				} else {
					setFailure(future.getCause());
				}
			}

		};

		final Listener listener = new Listener();

		void poll() {
			while (true) {
				Task task = taskList.peek();
				if (task == null) {
					setSuccess();
					return;
				}

				if (task.future != null) {
					taskList.remove();
					task.future.addListener(listener);
					return;
				} else if (task.callable != null) {
					// We don't remove a poll until it returns null
					ChannelFuture future;
					try {
						future = task.callable.call();
					} catch (Exception e) {
						setFailure(e);
						return;
					}
					if (future != null) {
						future.addListener(listener);
						return;
					} else {
						taskList.remove();
					}
				} else {
					throw new IllegalStateException();
				}
			}
		}

		public FutureChain poll(final Callable<ChannelFuture> callable) {
			Task task = new Task();
			task.callable = callable;
			taskList.add(task);

			return this;
		}

		public FutureChain then(ChannelFuture future) {
			if (future != null) {
				Task task = new Task();
				task.future = future;
				taskList.add(task);
			}

			return this;
		}

		public FutureChain poke() {
			poll();
			return this;
			//
			// Task task = taskList.peek();
			// if (task != null) {
			// if (task.callable != null) {
			// try {
			// task.callable.call();
			// } catch (Exception e) {
			// setFailure(e);
			// }
			// } else {
			// throw new IllegalStateException();
			// }
			// }
			//
			// return this;
		}
	}

	public static FutureChain on(Channel channel, boolean cancellable) {
		FutureChain poller = new FutureChain(channel, cancellable);
		return poller;
	}

	public static FutureChain on(Channel channel) {
		boolean cancellable = false;
		return on(channel, cancellable);
	}

}
