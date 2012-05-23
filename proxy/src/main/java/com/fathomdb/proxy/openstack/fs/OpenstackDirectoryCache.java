package com.fathomdb.proxy.openstack.fs;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.config.HasConfiguration;
import com.fathomdb.meta.Meta;
import com.fathomdb.proxy.http.client.ThreadPools;
import com.fathomdb.proxy.openstack.ListContainerObjectsOperation;
import com.fathomdb.proxy.openstack.OpenstackClientPool;
import com.fathomdb.proxy.openstack.OpenstackCredentials;
import com.fathomdb.proxy.openstack.OpenstackSession;

public class OpenstackDirectoryCache implements HasConfiguration {
	static final Logger log = LoggerFactory.getLogger(OpenstackDirectoryCache.class);

	private final OpenstackClientPool openstackClientPool;

	public OpenstackDirectoryCache(OpenstackClientPool openstackClientPool) {
		this.openstackClientPool = openstackClientPool;
	}

	public void initialize() {
		ThreadPools.SYSTEM_TASK_POOL.scheduleWithFixedDelay(new UpdateChecker(), UpdateChecker.INTERVAL,
				UpdateChecker.INTERVAL, TimeUnit.SECONDS);
	}

	class UpdateChecker implements Runnable {
		static final int INTERVAL = 300; // Five minutes

		@Override
		public void run() {
			try {
				refresh();
			} catch (Throwable t) {
				log.warn("Error on cache refresh task", t);
			}
		}

	};

	protected final AsyncCache<CacheKey, CacheEntry> cache = new AsyncCache<CacheKey, CacheEntry>() {

		@Override
		protected FetchOperation buildFetchOperation(CacheKey key) {
			return new DirectoryListingFetchOperation(key).start();
		}

		@Override
		public void refreshPeriodically(Collection<CacheKey> keys) {
			// TODO: Is this breaking LRU??

			log.info("Starting openstack container refresh");

			for (CacheKey key : keys) {
				FetchOperation operation = cache.getIfPresent(key);
				if (operation == null) {
					log.info("Key concurrently removed: " + key);
					continue;
				}

				if (operation.isInProgress()) {
					log.info("Operation in progress: " + key);
					continue;
				}

				log.info("Forcing refresh on: " + key.credentials.getUsername() + "::" + key.containerName);
				refresh(key);
			}
		}

		class DirectoryListingFetchOperation extends FetchOperation {
			public DirectoryListingFetchOperation(CacheKey key) {
				super(key);
			}

			ListContainerObjectsOperation listContainerObjects;

			OpenstackSession session;

			@Override
			protected CacheEntry doAsyncFetch() {
				if (session == null) {
					session = new OpenstackSession(openstackClientPool, key.credentials);
				}

				if (listContainerObjects == null) {
					listContainerObjects = new ListContainerObjectsOperation(session, key.containerName);
				}

				OpenstackItem item = listContainerObjects.get();
				return new CacheEntry(item);
			}

		}

	};

	public static class CacheKey {
		private static final Meta<CacheKey> META = Meta.get(CacheKey.class);

		public final OpenstackCredentials credentials;
		public final String containerName;

		public CacheKey(OpenstackCredentials credentials, String containerName) {
			super();
			this.credentials = credentials;
			this.containerName = containerName;
		}

		@Override
		public int hashCode() {
			return META.hashCode(this);
		}

		@Override
		public boolean equals(Object obj) {
			return META.equals(this, obj);
		}

		@Override
		public String toString() {
			return META.toString(this);
		}
	}

	public static class CacheEntry {
		public final OpenstackItem root;

		public CacheEntry(OpenstackItem root) {
			this.root = root;
		}
	}

	public OpenstackItem getAsync(OpenstackCredentials credentials, String containerName) {
		CacheKey key = new CacheKey(credentials, containerName);
		return cache.getAsync(key).root;
	}

	@Override
	public void refresh() {
		cache.refresh();
	}

}
