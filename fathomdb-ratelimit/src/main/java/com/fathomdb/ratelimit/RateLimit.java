package com.fathomdb.ratelimit;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.OperationFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.TimeSpan;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class RateLimit {
	private static final Logger log = LoggerFactory.getLogger(RateLimit.class);

	private final RateLimitSystem system;
	private final String prefix;

	final int limit;
	final int window;
	final int writeToMemcacheThreshold;
	final int readFromMemcacheThreshold;
	final LoadingCache<String, Counter> localCache;

	public static class AsyncIncrement {
		public final String key;
		int delta;

		public AsyncIncrement(String key, int delta) {
			this.key = key;
			this.delta = delta;
		}
	}

	final LinkedBlockingQueue<AsyncIncrement> queuedAdds;

	final Thread flushMemcacheThread;

	public RateLimit(RateLimitSystem system, String prefix, TimeSpan window,
			int limit, int writeToMemcacheThreshold) {
		super();
		this.system = system;
		this.prefix = prefix;
		this.limit = limit;
		this.window = (int) window.getTotalSeconds();
		this.writeToMemcacheThreshold = writeToMemcacheThreshold;
		this.readFromMemcacheThreshold = writeToMemcacheThreshold;

		this.localCache = CacheBuilder.newBuilder()
				.expireAfterWrite(window.getTotalSeconds(), TimeUnit.SECONDS)
				.build(new CacheLoader<String, Counter>() {
					@Override
					public Counter load(String key) throws Exception {
						return new Counter();
					}
				});

		int maxCapacity = 100000;
		this.queuedAdds = new LinkedBlockingQueue<AsyncIncrement>(maxCapacity);

		this.flushMemcacheThread = new Thread(new FlusherThreadRunnable(),
				"RateLimitMemcacheFlusher");
		flushMemcacheThread.setDaemon(true);
		flushMemcacheThread.start();
	}

	public RateLimit(RateLimitSystem system, String prefix, TimeSpan window,
			int limit) {
		this(system, prefix, window, limit, limit / 4);
	}

	static class Counter {
		public int count;
	}

	public boolean isOverLimit(String key) {
		String cacheKey = buildCacheKey(key);
		Counter counter = getCounter(cacheKey);

		int count;

		synchronized (counter) {
			count = counter.count;
		}

		if (count > limit) {
			return true;
		}

		if (count >= readFromMemcacheThreshold) {
			// Read the latest from memcache
			MemcachedClient client = system.getClient();
			String memcacheKey = buildMemcachePath(cacheKey);

			try {
				// We send a zero-increment; it avoids transcoding
				count = (int) client.incr(memcacheKey, 0, count, window);

				synchronized (counter) {
					if (count > counter.count) {
						counter.count = count;
					}
				}
			} catch (Exception e) {
				// If memcache fails, we'll just fall back to local counting
				log.warn("Error getting count from memcache", e);
			}
		}

		boolean overLimit = (count > limit);
		if (overLimit) {
			log.info("Counter is over rate limit: " + prefix + ":" + key);
		}
		return overLimit;
	}

	private Counter getCounter(String cacheKey) {
		// TODO: We could probably be much more efficient here by using our own
		// (less-general, more-aware) data structure
		Counter counter;
		try {
			counter = localCache.get(cacheKey);
		} catch (ExecutionException e) {
			throw new IllegalStateException("Error building counter", e);
		}
		return counter;
	}

	private String buildCacheKey(String key) {
		int now = Clock.getUnixTime();
		int bucket = now / window;

		String cacheKey = key + "\t" + bucket;
		return cacheKey;
	}

	public void add(String key, int delta) {
		String cacheKey = buildCacheKey(key);
		Counter counter = getCounter(cacheKey);

		int count;

		synchronized (counter) {
			count = counter.count + delta;
			counter.count = count;
		}

		try {
			if (count >= writeToMemcacheThreshold) {
				MemcachedClient client = system.getClient();
				String memcacheKey = buildMemcachePath(cacheKey);

				int memcacheDelta;

				if (count == writeToMemcacheThreshold) {
					memcacheDelta = count;
				} else {
					memcacheDelta = 1;
				}

				boolean ASYNC = true;
				if (ASYNC) {
					AsyncIncrement op = new AsyncIncrement(memcacheKey,
							memcacheDelta);
					if (!queuedAdds.offer(op)) {
						log.warn("Unable to add memcache add operation to queue");
					}
				} else {
					count = (int) client.incr(memcacheKey, memcacheDelta,
							count, window);

					synchronized (counter) {
						if (count > counter.count) {
							counter.count = count;
						}
					}
				}
			}
		} catch (Exception e) {
			// If memcache fails, we fall back to local counting
			log.warn("Error incrementing memcache counter", e);
		}
	}

	class FlusherThreadRunnable implements Runnable {

		private void flushQueue() throws InterruptedException {
			AsyncIncrement head = queuedAdds.take();

			List<AsyncIncrement> adds = Lists.newArrayList();
			adds.add(head);

			queuedAdds.drainTo(adds);

			Map<String, AsyncIncrement> addsByKey = Maps.newHashMap();
			for (AsyncIncrement add : adds) {
				AsyncIncrement existing = addsByKey.get(add.key);
				if (existing == null) {
					addsByKey.put(add.key, add);
				} else {
					existing.delta += add.delta;
				}
			}

			MemcachedClient client = system.getClient();

			List<OperationFuture<Long>> futures = Lists.newArrayList();
			for (AsyncIncrement add : addsByKey.values()) {
				OperationFuture<Long> future = client.asyncIncr(add.key,
						add.delta);
				futures.add(future);
			}

			for (OperationFuture<Long> future : futures) {
				try {
					future.get(1, TimeUnit.SECONDS);
				} catch (Exception e) {
					log.warn("Error flushing counts to memcache", e);
				}
			}
		}

		@Override
		public void run() {
			while (true) {
				try {
					flushQueue();
				} catch (Exception e) {
					log.warn("Error while flushing memcache queue", e);
				}
			}
		}
	}

	private String buildMemcachePath(String cacheKey) {
		String memcacheKey = prefix + "\t" + cacheKey;
		return memcacheKey;
	}

}
