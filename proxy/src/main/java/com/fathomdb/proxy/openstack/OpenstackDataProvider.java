package com.fathomdb.proxy.openstack;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

import com.fathomdb.proxy.cache.CacheFile;
import com.fathomdb.proxy.cache.CachingObjectDataSink;
import com.fathomdb.proxy.cache.HashKey;
import com.fathomdb.proxy.cache.ObjectDataSinkSplitter;
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

	final CacheFile cache;

	public OpenstackDataProvider(CacheFile cache,
			OpenstackCredentials openstackCredentials,
			OpenstackClientPool openstackClientPool) {
		this.cache = cache;
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

		class Resolved {
			String path;
			OpenstackItem pathItem;
			HttpResponse response;

			public Resolved(String path, OpenstackItem pathItem,
					HttpResponse response) {
				this.path = path;
				this.pathItem = pathItem;
				this.response = response;
			}
		}

		Resolved resolved;

		ServerRuleResolver serverRuleResolver;

		Resolved resolve() {
			if (resolved != null) {
				return resolved;
			}

			HttpMethod method = request.getMethod();

			if (method != HttpMethod.GET) {
				HttpResponse response = buildError(HttpResponseStatus.METHOD_NOT_ALLOWED);
				resolved = new Resolved(null, null, response);
				return resolved;
			}

			String path = request.getRequestURI();

			if (path.startsWith("/"))
				path = path.substring(1);

			String query;

			int questionIndex = path.indexOf('?');
			if (questionIndex != -1) {
				query = path.substring(questionIndex + 1);
				path = path.substring(0, questionIndex);
			} else {
				query = null;
			}

			OpenstackItem root = getDirectoryRoot();

			OpenstackItem pathItem = findItem(path);

			HttpResponse response = null;
			if (pathItem != null) {
				if (pathItem.isDirectory()) {
					if (!path.isEmpty() && !path.endsWith("/")) {
						// We need to send a redirect. See DirectorySlash in
						// Apache for a great explanation

						// The root (empty path) appears to be a special case

						response = buildError(HttpResponseStatus.MOVED_PERMANENTLY);
						// TODO: Does this need to be absolute??
						String redirectRelative = "/" + path + "/";
						// String redirectAbsolute =
						// request.toAbsolute(redirectRelative);
						response.setHeader(HttpHeaders.Names.LOCATION,
								redirectRelative);
					} else {
						boolean found = false;

						if (serverRuleResolver == null) {
							serverRuleResolver = new ServerRuleResolver(root);
						}

						ServerRuleChain rules = serverRuleResolver
								.resolveServerRules(path);
						for (String documentIndex : rules.getDocumentIndexes()) {
							OpenstackItem child = pathItem
									.getChild(documentIndex);
							if (child != null) {
								// Note that the rule chain is the same, because
								// it lives in the same directory!
								ruleChain = rules;

								path = path + documentIndex;
								pathItem = child;
								found = true;
								break;
							}
						}

						if (!found) {
							response = buildError(HttpResponseStatus.NOT_FOUND);
						}
					}
				}
			}

			resolved = new Resolved(path, pathItem, response);
			return resolved;
		}

		ServerRuleChain ruleChain;

		ServerRuleChain getRules() {
			if (ruleChain == null) {
				Resolved resolved = resolve();
				OpenstackItem root = getDirectoryRoot();

				if (serverRuleResolver == null) {
					serverRuleResolver = new ServerRuleResolver(root);
				}

				ServerRuleChain rules = serverRuleResolver
						.resolveServerRules(resolved.path);
				ruleChain = rules;
			}
			return ruleChain;
		}

		private HttpResponse buildError(HttpResponseStatus status) {
			DefaultHttpResponse response = new DefaultHttpResponse(
					HttpVersion.HTTP_1_1, status);

			// response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, 0);

			String responseBody = "Error " + status;
			response.setContent(ChannelBuffers.copiedBuffer(responseBody,
					CharsetUtil.UTF_8));
			response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
			response.setHeader(CONTENT_LENGTH, response.getContent()
					.readableBytes());

			return response;
		}

		OpenstackItem findItem(String path) {
			OpenstackItem root = getDirectoryRoot();

			OpenstackItem current = root;

			// We omit empty strings so we're not tricked by / or directory/
			// Also, we want directory and directory/ to be the same in terms of
			// resolution
			for (String pathToken : Splitter.on('/').omitEmptyStrings()
					.split(path)) {
				current = current.getChild(pathToken);
				if (current == null) {
					return null;
				}
			}
			return current;
		}

		HashKey getCacheKey(Resolved resolved) {
			if (resolved.pathItem == null)
				return null;
			return resolved.pathItem.getContentHash();
		}

		public boolean isStillValid(Resolved resolved, CacheLock found) {
			// We rely on a hashed file system for expiration etc
			return true;

			// if (found != null) {
			// // TODO: Check MD5
			// return true;
			// }
			//
			// return false;
		}

		ListContainerObjectsOperation listContainerObjects;

		private OpenstackItem getDirectoryRoot() {
			if (listContainerObjects == null) {
				listContainerObjects = new ListContainerObjectsOperation(
						session, getContainerName());
			}

			OpenstackItem root = listContainerObjects.get();
			return root;
		}

		private void sendCachedResponse(ObjectDataSink sink, Resolved resolved,
				CacheLock found) {
			ByteBuffer buffer = found.getBuffer();

			HttpResponse response = new DefaultHttpResponse(
					HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

			long contentLength = buffer.remaining();
			if (contentLength > 0) {
				response.setHeader(HttpHeaders.Names.CONTENT_LENGTH,
						contentLength);
			}

			String contentType = resolved.pathItem.getContentType();
			if (contentType != null) {
				response.setHeader(HttpHeaders.Names.CONTENT_TYPE, contentType);
			}

			getRules().addCacheHeaders(response);

			sink.beginResponse(response);
			if (contentLength > 0) {
				sink.gotData(ChannelBuffers.wrappedBuffer(buffer));
			}
			sink.endData();
		}

		private void sendResponse(ObjectDataSink sink, HttpResponse response) {
			ChannelBuffer content = response.getContent();

			sink.beginResponse(response);
			if (content != null && content.readable()) {
				sink.gotData(content);
			}
			sink.endData();
		}

		DownloadObjectOperation downloadOperation;

		boolean didCacheLookup;
		CacheLock found;

		@Override
		public void handle(ObjectDataSink sink) {
			Resolved resolved = resolve();

			if (resolved.response != null) {
				sendResponse(sink, resolved.response);
				return;
			}

			HashKey cacheKey = getCacheKey(resolved);

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
						log.info("Cache HIT on " + resolved.path + " "
								+ cacheKey);

						sendCachedResponse(sink, resolved, found);
						return;
					} else {
						log.info("Cache OUTOFDATE on " + resolved.path + " "
								+ cacheKey + " (hit but no longer valid)");
					}
				} finally {
					found.close();
				}
			}

			if (downloadOperation == null) {
				ObjectDataSink downloadTo = sink;
				if (cacheKey != null) {
					downloadTo = new ObjectDataSinkSplitter(
							new CachingObjectDataSink(cache, cacheKey),
							downloadTo);
				}

				HttpResponse response = new DefaultHttpResponse(
						HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

				String contentType = resolved.pathItem.getContentType();
				if (contentType != null) {
					response.setHeader(HttpHeaders.Names.CONTENT_TYPE,
							contentType);
				}

				getRules().addCacheHeaders(response);

				downloadOperation = new DownloadObjectOperation(session,
						getContainerName() + "/" + resolved.path, response,
						downloadTo);
			}
			downloadOperation.run();
		}

	}

	@Override
	public Handler buildHandler(GenericRequest request) {
		return new Handler(request);
	}
}
