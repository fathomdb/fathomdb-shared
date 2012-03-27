package com.fathomdb.proxy.openstack;

import org.apache.log4j.Logger;
import org.jboss.netty.handler.codec.http.HttpMethod;

import com.fathomdb.proxy.cache.CacheFile.CacheLock;
import com.fathomdb.proxy.http.server.GenericRequest;
import com.fathomdb.proxy.objectdata.ObjectDataProvider;
import com.fathomdb.proxy.objectdata.ObjectDataSink;
import com.fathomdb.proxy.openstack.fs.OpenstackItem;
import com.google.common.base.Splitter;

public class OpenstackDataProvider extends ObjectDataProvider {
	static final Logger log = Logger.getLogger(OpenstackDataProvider.class);

	final OpenstackClientPool openstackClientPool;

	final OpenstackCredentials openstackCredentials;

	final OpenstackSession session;

	public OpenstackDataProvider(OpenstackCredentials openstackCredentials,
			OpenstackClientPool openstackClientPool) {
		this.openstackCredentials = openstackCredentials;
		this.openstackClientPool = openstackClientPool;
		session = openstackClientPool.getClient(openstackCredentials);
	}

	String getContainerName() {
		String containerName = System.getProperty("container");
		if (containerName == null) {
			containerName = "bucketshop";
		}
		return containerName;
	}

	class Handler extends ObjectDataProvider.Handler {

		public Handler(GenericRequest request) {
			super(request);
		}

		ListContainerObjectsOperation listContainerObjects;
		
		@Override
		public boolean isStillValid(CacheLock found) {
			if (listContainerObjects == null) {
				listContainerObjects = new ListContainerObjectsOperation(session, getContainerName());
			}
			
			OpenstackItem root = listContainerObjects.get();

			String path = request.getRequestURI();

			if (path.startsWith("/"))
				path = path.substring(1);

			int questionIndex = path.indexOf('?');
			if (questionIndex != -1) {
				path = path.substring(0, questionIndex);
			}
			
			OpenstackItem current = root;
			for (String pathToken : Splitter.on('/').split(path)) {
				current = current.getChild(pathToken);
				if (current == null) {
					// TODO: Negative (404) caching?
					return false;
				}
			}

			if (current != null) {
				// TODO: Check MD5
				return true;
			}

			return false;
		}

		DownloadObjectOperation downloadOperation;

		@Override
		public void handle(ObjectDataSink sink) {
			final String requestURI = request.getRequestURI();
			String hostAndPort = request.getHeader("Host");
			HttpMethod method = request.getMethod();

			final String objectPath = getContainerName() + requestURI;

			log.debug("HandleRequest " + method + " " + hostAndPort + " "
					+ requestURI);

			// httpClientPool.getClient();

			int port = 0;

			int colonIndex = hostAndPort.indexOf(':');
			if (colonIndex != -1) {
			} else {
			}

			OpenstackStorageRequest upstreamRequest = null;

			if (method.equals(HttpMethod.GET)) {
				// Prepare the HTTP request.
				upstreamRequest = new OpenstackStorageRequest(objectPath);
			}

			if (upstreamRequest == null) {
				// TODO: Send a nice error
				throw new UnsupportedOperationException();
			}

			if (downloadOperation == null) {
				downloadOperation = new DownloadObjectOperation(session,
						objectPath, sink);
			}
			downloadOperation.run();
		}

	}

	@Override
	public Handler buildHandler(GenericRequest request) {
		return new Handler(request);
	}
}
