package com.fathomdb.ratelimit;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.TimeSpan;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class RateLimit {
	private static final Logger log = LoggerFactory.getLogger(RateLimit.class);

	private final RateLimitSystem system;
	private final String prefix;

	final int limit;
	final int window;
	final int writeToMemcacheThreshold;
	final int readFromMemcacheThreshold;
	final LoadingCache<String, Counter> localCache;

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
				if (count == writeToMemcacheThreshold) {
					// We send a zero-increment; it avoids transcoding
					count = (int) client.incr(memcacheKey, 0, count, window);
				}

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

		return count > limit;
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
			// TODO: Async/batch version
			if (count >= writeToMemcacheThreshold) {
				MemcachedClient client = system.getClient();
				String memcacheKey = buildMemcachePath(cacheKey);

				// TODO: Make this async?
				if (count == writeToMemcacheThreshold) {
					count = (int) client
							.incr(memcacheKey, count, count, window);
				} else {
					count = (int) client.incr(memcacheKey, 1, count, window);
				}

				synchronized (counter) {
					if (count > counter.count) {
						counter.count = count;
					}
				}
			}
		} catch (Exception e) {
			// If memcache fails, we fall back to local counting
			log.warn("Error incrementing memcache counter", e);
		}
	}

	private String buildMemcachePath(String cacheKey) {
		String memcacheKey = prefix + "\t" + cacheKey;
		return memcacheKey;
	}

}
