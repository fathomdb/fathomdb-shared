package com.fathomdb.proxy.objectdata;

import java.nio.ByteBuffer;
import java.util.Date;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.openstack.crypto.ByteString;

import com.fathomdb.cache.CacheFile.CacheLock;
import com.fathomdb.proxy.http.Dates;
import com.fathomdb.proxy.http.config.HostConfig;
import com.fathomdb.proxy.http.handlers.ContentType;
import com.fathomdb.proxy.http.rules.ServerRuleChain;
import com.fathomdb.proxy.http.rules.ServerRuleResolver;
import com.fathomdb.proxy.http.server.GenericRequest;
import com.fathomdb.proxy.http.vfs.VfsItem;
import com.fathomdb.proxy.http.vfs.VfsItemResolver;
import com.fathomdb.proxy.http.vfs.VfsItemResolver.Resolved;

public abstract class ObjectDataProvider {
	public abstract Handler buildHandler(GenericRequest request);

	public abstract class Handler implements AutoCloseable {
		protected final GenericRequest request;

		public Handler(GenericRequest request) {
			this.request = request;
		}

		public abstract void handle(ObjectDataSink sink) throws Exception;
	}

	public abstract class HandlerBase extends Handler {
		public HandlerBase(GenericRequest request) {
			super(request);
		}

		ServerRuleResolver serverRuleResolver;

		public ServerRuleResolver getServerRuleResolver() {
			if (serverRuleResolver == null) {
				serverRuleResolver = new ServerRuleResolver(getDirectoryRoot());
			}
			return serverRuleResolver;

		}

		VfsItemResolver resolver;

		public Resolved resolveToItem() {
			if (resolver == null) {
				resolver = new VfsItemResolver(request, getDirectoryRoot(), getServerRuleResolver());
			}
			return resolver.resolve();
		}

		protected ServerRuleChain getRules() {
			Resolved resolved = resolveToItem();
			return resolved.ruleChain;
		}

		protected ByteString getCacheKey(Resolved resolved) {
			if (resolved.pathItem == null) {
				return null;
			}
			return resolved.pathItem.getContentHash();
		}

		protected abstract VfsItem getDirectoryRoot();

		protected void sendCachedResponse(ObjectDataSink sink, Resolved resolved, CacheLock found) {
			ByteBuffer buffer = found.getBuffer();

			HttpResponse response = StandardResponses.buildResponse(request, HttpResponseStatus.OK);

			long contentLength = buffer.remaining();
			if (contentLength > 0) {
				response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, contentLength);
			}

			ContentType contentType = resolved.pathItem.getContentType();
			if (contentType != null) {
				response.setHeader(HttpHeaders.Names.CONTENT_TYPE, contentType.getContentType());
			}

			Date lastModified = resolved.pathItem.getLastModified();
			if (lastModified != null) {
				response.setHeader(HttpHeaders.Names.LAST_MODIFIED, Dates.format(lastModified));
			}

			getRules().addCacheHeaders(response);

			sink.beginResponse(response);
			if (contentLength > 0) {
				sink.gotData(ChannelBuffers.wrappedBuffer(buffer), true);
			}
			sink.endData();
		}

		protected void sendResponse(ObjectDataSink sink, HttpResponse response) {
			// ChannelBuffer content = response.getContent();

			sink.beginResponse(response);
			// if (content != null && content.readable()) {
			// sink.gotData(content);
			// }
			sink.endData();
		}

		protected boolean isStillValid(Resolved resolved, CacheLock found) {
			// We rely on a hashed file system for expiration etc
			return true;

			// if (found != null) {
			// // TODO: Check MD5
			// return true;
			// }
			//
			// return false;
		}

	}

	public void initialize(HostConfig hostConfig) {
	}
}
