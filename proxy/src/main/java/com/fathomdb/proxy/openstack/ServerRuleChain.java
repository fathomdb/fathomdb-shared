package com.fathomdb.proxy.openstack;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.fathomdb.proxy.htaccess.Directive;
import com.fathomdb.proxy.htaccess.DirectoryIndexDirective;
import com.fathomdb.proxy.htaccess.ExpirationDirective.ExpirationTimeout;
import com.fathomdb.proxy.htaccess.ExpiresByType;
import com.fathomdb.proxy.htaccess.ScopeDirective;
import com.fathomdb.proxy.http.Dates;
import com.fathomdb.proxy.http.handlers.ContentType;

public class ServerRuleChain {
	private final List<ScopeDirective> rules;

	public ServerRuleChain(List<ScopeDirective> rules) {
		this.rules = rules;
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

	public void addCacheHeaders(HttpResponse response) {
		ContentType responseContentType = ContentType.get(response);

		ExpiresByType lowest = null;
		// TODO: Hack. We need to walk the whole tree, for one thing, not just 2
		// levels
		for (ScopeDirective rule : rules) {
			for (Directive child : rule.getChildren()) {
				if (child instanceof ExpiresByType) {
					ExpiresByType expiresByType = (ExpiresByType) child;
					if (expiresByType.getContentType() == responseContentType) {
						lowest = expiresByType;
					}
				}
			}
		}

		if (lowest == null) {
			return;
		}

		long now = System.currentTimeMillis() / 1000L;
		long timeoutAt;

		ExpirationTimeout timeout = lowest.getTimeout();
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
