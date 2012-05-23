package com.fathomdb.proxy.openstack;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultCookie;
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
import com.fathomdb.proxy.http.Dates;
import com.fathomdb.proxy.http.handlers.ContentType;
import com.fathomdb.proxy.http.server.GenericRequest;
import com.fathomdb.proxy.objectdata.ObjectDataProvider;
import com.fathomdb.proxy.objectdata.ObjectDataSink;
import com.fathomdb.proxy.openstack.fs.OpenstackDirectoryCache;
import com.fathomdb.proxy.openstack.fs.OpenstackItem;
import com.google.common.base.Splitter;

public class OpenstackDataProvider extends ObjectDataProvider {
	static final Logger log = LoggerFactory
			.getLogger(OpenstackDataProvider.class);

	final OpenstackDirectoryCache openstackDirectoryCache;

	final OpenstackClientPool openstackClientPool;

	final OpenstackCredentials openstackCredentials;

	final CacheFile cache;

	final String containerName;

	public OpenstackDataProvider(
			OpenstackDirectoryCache openstackDirectoryCache, CacheFile cache,
			OpenstackCredentials openstackCredentials,
			OpenstackClientPool openstackClientPool, String containerName) {
		this.openstackDirectoryCache = openstackDirectoryCache;
		this.cache = cache;
		this.openstackCredentials = openstackCredentials;
		this.openstackClientPool = openstackClientPool;
		this.containerName = containerName;
	}

	String getContainerName() {
		return containerName;
	}

	class Handler extends ObjectDataProvider.Handler {
		public Handler(GenericRequest request) {
			super(request);
		}

		// TODO: Can we re-use auth by moving this up.
		// We don't want to reuse the swift connection though...
		OpenstackSession openstackSession;

		OpenstackSession getOpenstackSession() {
			if (openstackSession == null) {
				openstackSession = openstackClientPool
						.getClient(openstackCredentials);
			}
			return openstackSession;
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

			String path = request.getUri();

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

			if (pathItem == null) {
				response = buildError(HttpResponseStatus.NOT_FOUND);
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
			HttpResponse response = buildResponse(status);

			// response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, 0);

			String responseBody = "Error " + status;
			response.setContent(ChannelBuffers.copiedBuffer(responseBody,
					CharsetUtil.UTF_8));
			response.setHeader(HttpHeaders.Names.CONTENT_TYPE,
					"text/plain; charset=UTF-8");
			response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, response
					.getContent().readableBytes());

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

		OpenstackItem directoryRoot;

		private OpenstackItem getDirectoryRoot() {
			if (directoryRoot == null) {
				directoryRoot = openstackDirectoryCache.getAsync(
						openstackCredentials, getContainerName());
			}

			return directoryRoot;
		}

		private void sendCachedResponse(ObjectDataSink sink, Resolved resolved,
				CacheLock found) {
			ByteBuffer buffer = found.getBuffer();

			HttpResponse response = buildResponse(HttpResponseStatus.OK);

			long contentLength = buffer.remaining();
			if (contentLength > 0) {
				response.setHeader(HttpHeaders.Names.CONTENT_LENGTH,
						contentLength);
			}

			ContentType contentType = resolved.pathItem.getContentType();
			if (contentType != null) {
				response.setHeader(HttpHeaders.Names.CONTENT_TYPE, contentType.getContentType());
			}

			Date lastModified = resolved.pathItem.getLastModified();
			if (lastModified != null) {
				response.setHeader(HttpHeaders.Names.LAST_MODIFIED,
						Dates.format(lastModified));
			}

			getRules().addCacheHeaders(response);

			sink.beginResponse(response);
			if (contentLength > 0) {
				sink.gotData(ChannelBuffers.wrappedBuffer(buffer), true);
			}
			sink.endData();
		}

		private void sendResponse(ObjectDataSink sink, HttpResponse response) {
			// ChannelBuffer content = response.getContent();

			sink.beginResponse(response);
			// if (content != null && content.readable()) {
			// sink.gotData(content);
			// }
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

				HttpResponse response = buildResponse(HttpResponseStatus.OK);

				ContentType contentType = resolved.pathItem.getContentType();
				if (contentType != null) {
					response.setHeader(HttpHeaders.Names.CONTENT_TYPE,
							contentType.getContentType());
				}

				Date lastModified = resolved.pathItem.getLastModified();
				if (lastModified != null) {
					response.setHeader(HttpHeaders.Names.LAST_MODIFIED,
							Dates.format(lastModified));
				}

				getRules().addCacheHeaders(response);

				downloadOperation = new DownloadObjectOperation(
						getOpenstackSession(), getContainerName() + "/"
								+ resolved.path, response, downloadTo);
			}
			downloadOperation.doDownload();
		}

		public void close() {
			if (openstackSession != null) {
				openstackSession.close();
			}
		}

		static final int SECONDS_IN_MINUTE = 60;
		static final int SECONDS_IN_HOUR = SECONDS_IN_MINUTE * 60;
		static final int SECONDS_IN_DAY = SECONDS_IN_HOUR * 24;
		static final int SECONDS_IN_YEAR = SECONDS_IN_DAY * 365;
		static final int SECONDS_IN_DECADE = 10 * SECONDS_IN_YEAR;

		private HttpResponse buildResponse(HttpResponseStatus status) {
			HttpResponse response = new DefaultHttpResponse(
					HttpVersion.HTTP_1_1, status);

			// Use cookies to allow tracking sessions in the debug log
			String cookieName = "uuid";
			boolean found = false;
			String cookieString = request.getHeader(HttpHeaders.Names.COOKIE);
			if (cookieString != null) {
				CookieDecoder cookieDecoder = new CookieDecoder();
				Set<Cookie> cookies = cookieDecoder.decode(cookieString);
				if (!cookies.isEmpty()) {
					for (Cookie cookie : cookies) {
						if (cookie.getName().equals(cookieName)) {
							found = true;
						}
					}
				}
			}

			if (!found) {
				CookieEncoder cookieEncoder = new CookieEncoder(true);
				Cookie cookie = new DefaultCookie(cookieName, UUID.randomUUID()
						.toString());
				cookie.setMaxAge(SECONDS_IN_DECADE);

				cookieEncoder.addCookie(cookie);
				response.addHeader(HttpHeaders.Names.SET_COOKIE,
						cookieEncoder.encode());
			}

			return response;
		}

	}

	@Override
	public Handler buildHandler(GenericRequest request) {
		return new Handler(request);
	}
}
