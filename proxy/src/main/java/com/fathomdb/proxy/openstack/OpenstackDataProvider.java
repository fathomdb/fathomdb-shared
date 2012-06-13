package com.fathomdb.proxy.openstack;

import java.util.Date;

import javax.inject.Inject;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.openstack.crypto.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.cache.Cache;
import com.fathomdb.cache.CacheFile.CacheLock;
import com.fathomdb.meta.Meta;
import com.fathomdb.proxy.cache.CachingObjectDataSink;
import com.fathomdb.proxy.cache.ObjectDataSinkSplitter;
import com.fathomdb.proxy.http.Dates;
import com.fathomdb.proxy.http.config.HostConfig;
import com.fathomdb.proxy.http.handlers.ContentType;
import com.fathomdb.proxy.http.server.GenericRequest;
import com.fathomdb.proxy.http.vfs.VfsItem;
import com.fathomdb.proxy.http.vfs.VfsItemResolver.Resolved;
import com.fathomdb.proxy.objectdata.ObjectDataProvider;
import com.fathomdb.proxy.objectdata.ObjectDataSink;
import com.fathomdb.proxy.objectdata.StandardResponses;
import com.fathomdb.proxy.openstack.fs.OpenstackDirectoryCache;

public class OpenstackDataProvider extends ObjectDataProvider {
	static final Logger log = LoggerFactory.getLogger(OpenstackDataProvider.class);

	@Inject
	OpenstackDirectoryCache openstackDirectoryCache;

	@Inject
	OpenstackClientPool openstackClientPool;

	@Inject
	Cache cache;

	String containerName;
	OpenstackCredentials openstackCredentials;

	@Override
	public void initialize(HostConfig config) {
		containerName = config.getContainerName();
		openstackCredentials = config.getOpenstackCredentials();
	}

	String getContainerName() {
		return containerName;
	}

	static final Meta<Handler> META_Handler = Meta.get(Handler.class);

	class Handler extends ObjectDataProvider.HandlerBase {
		public Handler(GenericRequest request) {
			super(request);
		}

		// TODO: Can we re-use auth by moving this up.
		// We don't want to reuse the swift connection though...
		OpenstackSession openstackSession;

		OpenstackSession getOpenstackSession() {
			if (openstackSession == null) {
				openstackSession = openstackClientPool.getClient(openstackCredentials);
			}
			return openstackSession;
		}

		VfsItem directoryRoot;

		@Override
		protected VfsItem getDirectoryRoot() {
			if (directoryRoot == null) {
				directoryRoot = openstackDirectoryCache.getAsync(openstackCredentials, getContainerName());
			}

			return directoryRoot;
		}

		DownloadObjectOperation downloadOperation;

		boolean didCacheLookup;
		CacheLock found;

		@Override
		public void handle(ObjectDataSink sink) {
			Resolved resolved = resolveToItem();

			if (resolved.response != null) {
				sendResponse(sink, resolved.response);
				return;
			}

			ByteString cacheKey = getCacheKey(resolved);

			if (!didCacheLookup && cacheKey != null) {
				didCacheLookup = true;

				found = cache.lookup(cacheKey);
				if (found == null) {
					log.info("Cache MISS on " + resolved.path + " " + cacheKey);
				}
			}

			if (found != null) {
				try {
					if (isStillValid(resolved, found)) {
						log.info("Cache HIT on " + resolved.path + " " + cacheKey);

						sendCachedResponse(sink, resolved, found);
						return;
					} else {
						log.info("Cache OUTOFDATE on " + resolved.path + " " + cacheKey + " (hit but no longer valid)");
					}
				} finally {
					found.close();
				}
			}

			if (downloadOperation == null) {
				ObjectDataSink downloadTo = sink;
				if (cacheKey != null) {
					downloadTo = new ObjectDataSinkSplitter(new CachingObjectDataSink(cache, cacheKey), downloadTo);
				}

				HttpResponse response = StandardResponses.buildResponse(request, HttpResponseStatus.OK);

				ContentType contentType = resolved.pathItem.getContentType();
				if (contentType != null) {
					response.setHeader(HttpHeaders.Names.CONTENT_TYPE, contentType.getContentType());
				}

				Date lastModified = resolved.pathItem.getLastModified();
				if (lastModified != null) {
					response.setHeader(HttpHeaders.Names.LAST_MODIFIED, Dates.format(lastModified));
				}

				getRules().addCacheHeaders(response);

				downloadOperation = new DownloadObjectOperation(getOpenstackSession(), getContainerName() + "/"
						+ resolved.path, response, downloadTo);
			}
			downloadOperation.doDownload();
		}

		@Override
		public void close() {
			META_Handler.closeAll(this);
		}
	}

	@Override
	public Handler buildHandler(GenericRequest request) {
		return new Handler(request);
	}

}
