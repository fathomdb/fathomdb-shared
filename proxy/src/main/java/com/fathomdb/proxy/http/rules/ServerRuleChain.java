package com.fathomdb.proxy.http.rules;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.fathomdb.proxy.htaccess.Directive;
import com.fathomdb.proxy.htaccess.DirectoryIndexDirective;
import com.fathomdb.proxy.htaccess.ScopeDirective;
import com.fathomdb.proxy.htaccess.expires.ExpirationDirective.ExpirationTimeout;
import com.fathomdb.proxy.http.Dates;
import com.fathomdb.proxy.http.vfs.VfsItemResolver.Resolved;

/*
 * The list of .htaccess files that apply, starting from the root and working down
 */
public class ServerRuleChain {
	private final List<ScopeDirective> rules;

	public ServerRuleChain(List<ScopeDirective> rules) {
		this.rules = rules;
	}

	public void accept(ServerRuleVisitor visitor) {
		visitor.visit(this);
		for (ScopeDirective rule : rules) {
			rule.accept(visitor);
		}
	}

	public Iterable<String> getDocumentIndexes() {
		DirectoryIndexDirective lowest = null;
		// TODO: Hack. We need to walk the whole tree, for one thing, not just 2
		// levels
		for (ScopeDirective rule : rules) {
			for (Directive child : rule.getChildren()) {
				if (child instanceof DirectoryIndexDirective) {
					lowest = (DirectoryIndexDirective) child;
				}
			}
		}

		if (lowest == null) {
			return Collections.emptyList();
		}

		return lowest.getDirectoryIndexUrls();
	}

	public void addCacheHeaders(Resolved resolved, HttpResponse response) {
		ExpirationVisitor visitor = new ExpirationVisitor(resolved, response);
		accept(visitor);

		ExpirationTimeout timeout = visitor.getExpirationTimeout();
		if (timeout == null) {
			return;
		}

		long now = System.currentTimeMillis() / 1000L;
		long timeoutAt;

		switch (timeout.getBase()) {
		case Access:
		case Now:
			timeoutAt = now + timeout.getAddSeconds();
			break;

		default:
			throw new UnsupportedOperationException();
		}

		long deltaSeconds = timeoutAt - now;

		String cacheControl = response.getHeader(HttpHeaders.Names.CACHE_CONTROL);
		if (cacheControl == null) {
			cacheControl = "max-age=" + deltaSeconds;
		} else {
			throw new UnsupportedOperationException();
		}

		response.setHeader(HttpHeaders.Names.CACHE_CONTROL, cacheControl);

		String expires = Dates.format(new Date(timeoutAt * 1000L));

		response.setHeader(HttpHeaders.Names.EXPIRES, expires);
	}
}
