package com.fathomdb.proxy.openstack.fs;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.fathomdb.proxy.http.client.ThreadPools;
import com.fathomdb.proxy.openstack.ListContainerObjectsOperation;
import com.fathomdb.proxy.openstack.OpenstackClientPool;
import com.fathomdb.proxy.openstack.OpenstackCredentials;
import com.fathomdb.proxy.openstack.OpenstackSession;

public class OpenstackDirectoryCache {
	static final Logger log = Logger.getLogger(OpenstackDirectoryCache.class);

	private final OpenstackClientPool openstackClientPool;

	public OpenstackDirectoryCache(OpenstackClientPool openstackClientPool) {
		this.openstackClientPool = openstackClientPool;
	}

	public void initialize() {
		ThreadPools.SYSTEM_TASK_POOL.scheduleWithFixedDelay(
				new UpdateChecker(), UpdateChecker.INTERVAL,
				UpdateChecker.INTERVAL, TimeUnit.SECONDS);
	}

	class UpdateChecker implements Runnable {
		static final int INTERVAL = 30;

		@Override
		public void run() {
			try {
				cache.refresh();
			} catch (Throwable t) {
				log.warn("Error on cache refresh task task", t);
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
					session = new OpenstackSession(openstackClientPool,
							key.credentials);
				}

				if (listContainerObjects == null) {
					listContainerObjects = new ListContainerObjectsOperation(
							session, key.containerName);
				}

				OpenstackItem item = listContainerObjects.get();
				return new CacheEntry(item);
			}

		}

	};

	public static class CacheKey {
		public final OpenstackCredentials credentials;
		public final String containerName;

		public CacheKey(OpenstackCredentials credentials, String containerName) {
			super();
			this.credentials = credentials;
			this.containerName = containerName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((containerName == null) ? 0 : containerName.hashCode());
			result = prime * result
					+ ((credentials == null) ? 0 : credentials.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CacheKey other = (CacheKey) obj;
			if (containerName == null) {
				if (other.containerName != null)
					return false;
			} else if (!containerName.equals(other.containerName))
				return false;
			if (credentials == null) {
				if (other.credentials != null)
					return false;
			} else if (!credentials.equals(other.credentials))
				return false;
			return true;
		}

	}

	public static class CacheEntry {
		public final OpenstackItem root;

		public CacheEntry(OpenstackItem root) {
			this.root = root;
		}
	}

	public OpenstackItem getAsync(OpenstackCredentials credentials,
			String containerName) {
		CacheKey key = new CacheKey(credentials, containerName);
		return cache.getAsync(key).root;
	}

}
