package com.fathomdb.proxy.openstack;

import java.net.URI;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import com.fathomdb.proxy.openstack.fs.OpenstackDirectoryBuilder;
import com.fathomdb.proxy.openstack.fs.OpenstackDirectoryCache;
import com.fathomdb.proxy.openstack.fs.OpenstackDirectoryCache.CacheEntry;
import com.fathomdb.proxy.openstack.fs.OpenstackItem;

public class ListContainerObjectsOperation {
	final OpenstackSession session;
	final String path;

	public ListContainerObjectsOperation(OpenstackSession session, String path) {
		this.session = session;
		this.path = path;
	}

	CacheEntry cacheEntry;
	String cacheKey;
	ContainerListResponseHandler containerListingResponse;

	public OpenstackItem get() throws AsyncFutureException {
		if (cacheKey == null) {
			// TODO: HACK!
			cacheKey = path;
			OpenstackDirectoryCache cache = OpenstackDirectoryCache.INSTANCE;
			this.cacheEntry = cache.find(cacheKey);
		}

		if (cacheEntry != null) {
			return cacheEntry.root;
		}

		if (containerListingResponse == null) {
			String containerName = path;
			if (containerName.startsWith("/"))
				containerName = containerName.substring(1);
			int slashIndex = containerName.indexOf('/');
			if (slashIndex != -1) {
				containerName = containerName.substring(0, slashIndex);
			}

			SwiftClient swift = session.getSwiftClient();
			URI swiftUrl = swift.getBaseUrl();
			String fullPath = swiftUrl.getPath();
			fullPath += "/" + containerName + "/";

			HttpRequest request = swift.buildRequest(HttpMethod.GET, fullPath);
			request.setHeader("X-Auth-Token", session.getAuthTokenId());

			request.setHeader(HttpHeaders.Names.CONTENT_TYPE,
					"application/json");
			request.setHeader(HttpHeaders.Names.ACCEPT, "application/json");

			OpenstackDirectoryBuilder directoryListing = new OpenstackDirectoryBuilder();
			ContainerListResponseHandler responseHandler = new ContainerListResponseHandler(
					directoryListing);
			containerListingResponse = swift
					.doRequest(request, responseHandler);

			throw new AsyncFutureException(containerListingResponse.getFuture(), "Swift container listing");
		}

		OpenstackItem root = (OpenstackItem) containerListingResponse
				.getResult();
		cacheEntry = new CacheEntry(root);
		OpenstackDirectoryCache.INSTANCE.put(cacheKey, cacheEntry);

		return root;
	}

}
