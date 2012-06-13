package com.fathomdb.proxy.http.vfs;

import java.io.Closeable;
import java.io.IOException;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.fathomdb.meta.Meta;
import com.fathomdb.proxy.http.HttpException;
import com.fathomdb.proxy.http.rules.ServerRuleChain;
import com.fathomdb.proxy.http.rules.ServerRuleResolver;
import com.fathomdb.proxy.http.server.GenericRequest;
import com.fathomdb.proxy.objectdata.StandardResponses;
import com.google.common.base.Splitter;

public class VfsItemResolver implements Closeable {
	static final Meta<VfsItemResolver> META = Meta.get(VfsItemResolver.class);

	final GenericRequest request;
	final VfsItem root;
	final ServerRuleResolver serverRuleResolver;

	public VfsItemResolver(GenericRequest request, VfsItem root, ServerRuleResolver serverRuleResolver) {
		this.request = request;
		this.root = root;
		this.serverRuleResolver = serverRuleResolver;
	}

	public class Resolved {
		public final String path;
		public final VfsItem pathItem;
		public final HttpResponse response;
		public final ServerRuleChain ruleChain;

		public Resolved(String path, VfsItem pathItem, HttpResponse response, ServerRuleChain ruleChain) {
			this.path = path;
			this.pathItem = pathItem;
			this.response = response;
			this.ruleChain = ruleChain;
		}
	}

	public String getPath() throws HttpException {
		HttpMethod method = request.getMethod();

		if (method != HttpMethod.GET) {
			throw HttpException.METHOD_NOT_ALLOWED;
		}

		String path = request.getUri();

		if (path.startsWith("/")) {
			path = path.substring(1);
		}

		String query;

		int questionIndex = path.indexOf('?');
		if (questionIndex != -1) {
			query = path.substring(questionIndex + 1);
			path = path.substring(0, questionIndex);
		} else {
			query = null;
		}

		return path;
	}

	Resolved resolved;

	public Resolved resolve() {
		if (resolved != null) {
			return resolved;
		}

		String path = getPath();

		VfsItem pathItem = findItem(path);
		ServerRuleChain ruleChain = null;

		HttpResponse response = null;
		if (pathItem != null) {
			if (pathItem.isDirectory()) {
				if (!path.isEmpty() && !path.endsWith("/")) {
					// We need to send a redirect. See DirectorySlash in
					// Apache for a great explanation

					// The root (empty path) appears to be a special case

					// TODO: Move to exception, then remove response from resolver?
					response = StandardResponses.buildErrorResponse(request, HttpResponseStatus.MOVED_PERMANENTLY);
					// TODO: Does this need to be absolute??
					String redirectRelative = "/" + path + "/";
					// String redirectAbsolute =
					// request.toAbsolute(redirectRelative);
					response.setHeader(HttpHeaders.Names.LOCATION, redirectRelative);
				} else {
					boolean found = false;

					ServerRuleChain rules = serverRuleResolver.resolveServerRules(path);
					for (String documentIndex : rules.getDocumentIndexes()) {
						VfsItem child = pathItem.findChild(documentIndex);
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
						throw HttpException.NOT_FOUND;
					}
				}
			} else {
				ruleChain = serverRuleResolver.resolveServerRules(path);
			}
		}

		if (pathItem == null) {
			throw HttpException.NOT_FOUND;
		}

		resolved = new Resolved(path, pathItem, response, ruleChain);
		return resolved;
	}

	VfsItem findItem(String path) {
		VfsItem current = root;

		// We omit empty strings so we're not tricked by / or directory/
		// Also, we want directory and directory/ to be the same in terms of
		// resolution
		for (String pathToken : Splitter.on('/').omitEmptyStrings().split(path)) {
			current = current.getChild(pathToken);
			if (current == null) {
				return null;
			}
		}
		return current;
	}

	@Override
	public void close() throws IOException {
		META.closeAll(this);
	}
}
