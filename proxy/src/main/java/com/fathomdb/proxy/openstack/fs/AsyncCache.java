package com.fathomdb.proxy.openstack.fs;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.DefaultChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.proxy.openstack.AsyncFutureException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

public abstract class AsyncCache<K, V> {
	static final Logger log = LoggerFactory.getLogger(AsyncCache.class);

	protected final LoadingCache<K, FetchOperation> cache = CacheBuilder.newBuilder().build(
			new CacheLoader<K, FetchOperation>() {

				@Override
				public FetchOperation load(K key) throws Exception {
					return buildFetchOperation(key);
				}
			});

	abstract class FetchOperation {
		boolean done;

		protected final K key;
		V value;

		// ChannelFuture currentOperationFuture;

		ChannelFuture fetchOperationFuture;

		protected FetchOperation(K key) {
			this.key = key;
		}

		protected abstract V doAsyncFetch();

		class Listener implements ChannelFutureListener {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					try {
						poll();
					} catch (AsyncFutureException e) {
						log.info("Resubscribing on " + e.getMessage());
						e.getFuture().addListener(this);
					}
				} else {
					fetchOperationFuture.setFailure(future.getCause());
				}
			}

		};

		final Listener listener = new Listener();

		void poll() {
			synchronized (this) {
				value = doAsyncFetch();
				// Success!!
				done = true;
				fetchOperationFuture.setSuccess();
			}
		}

		FetchOperation start() {
			if (fetchOperationFuture != null) {
				throw new IllegalStateException();
			}

			Channel channel = null;
			boolean cancellable = false;
			fetchOperationFuture = new DefaultChannelFuture(channel, cancellable);

			try {
				poll();
			} catch (AsyncFutureException e) {
				e.getFuture().addListener(listener);

				// Don't throw here; we are just building the operation, not
				// asking for the value
			}

			return this;
		}

		V getAsync() {
			synchronized (this) {
				if (done) {
					return value;
				}

				if (fetchOperationFuture != null) {
					throw new AsyncFutureException(fetchOperationFuture, "Async cache fetch");
				}

				throw new IllegalStateException();
			}
		}

		public boolean isInProgress() {
			synchronized (this) {
				return !done;
			}
		}

		public void addListener(ChannelFutureListener listener) {
			synchronized (this) {
				if (fetchOperationFuture != null) {
					fetchOperationFuture.addListener(listener);
				} else {
					throw new IllegalStateException();
				}
			}
		}
	}

	protected abstract FetchOperation buildFetchOperation(K key);

	public V getAsync(K key) {
		FetchOperation operation;
		try {
			operation = cache.get(key);
		} catch (ExecutionException e) {
			throw new IllegalStateException("Error fetching from cache", e);
		}

		return operation.getAsync();
	}

	protected void refreshPeriodically(Collection<K> keys) {
	}

	public void refresh() {
		Collection<K> keySnapshot = Lists.newArrayList(cache.asMap().keySet());
		refreshPeriodically(keySnapshot);
	}

	protected void refresh(final K key) {
		// A cache.refresh wouldn't do what we want, because we want the
		// operation to complete before we put it into the cache

		// So we create the operation, wait for it to complete, and then insert
		// it
		final FetchOperation operation = buildFetchOperation(key);

		operation.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					cache.put(key, operation);
					log.info("Inserting updated version into cache: " + key);
				} else {
					Throwable t = future.getCause();

					log.warn("Error while background-refreshing cache: " + key, t);
				}
			}
		});
	}

}
