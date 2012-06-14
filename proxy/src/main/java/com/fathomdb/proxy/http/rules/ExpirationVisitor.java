package com.fathomdb.proxy.http.rules;

import org.jboss.netty.handler.codec.http.HttpResponse;

import com.fathomdb.proxy.htaccess.expires.ExpirationDirective.ExpirationTimeout;
import com.fathomdb.proxy.htaccess.expires.ExpiresActive;
import com.fathomdb.proxy.htaccess.expires.ExpiresByType;
import com.fathomdb.proxy.htaccess.expires.ExpiresDefault;
import com.fathomdb.proxy.http.handlers.ContentType;
import com.fathomdb.proxy.http.vfs.VfsItemResolver.Resolved;

public class ExpirationVisitor extends ServerRuleVisitorBase {
	final ContentType responseContentType;

	ExpiresDefault expiresDefault;
	ExpiresByType expiresByType;
	ExpiresActive expiresActive;

	public ExpirationVisitor(Resolved resolved, HttpResponse response) {
		super(resolved, response);
		this.responseContentType = ContentType.get(response);
	}

	public ExpirationTimeout getExpirationTimeout() {
		if (expiresActive != null && expiresActive.isActive()) {
			if (expiresByType != null) {
				return expiresByType.getTimeout();
			} else if (expiresDefault != null) {
				return expiresDefault.getTimeout();
			}
		}

		return null;

	}

	@Override
	public void visit(ExpiresActive expiresActive) {
		this.expiresActive = expiresActive;
	}

	@Override
	public void visit(ExpiresByType expiresByType) {
		if (expiresByType.getContentType() == responseContentType) {
			this.expiresByType = expiresByType;
		}
	}

	@Override
	public void visit(ExpiresDefault expiresDefault) {
		// Though this isn't clear, we reset all existing ExpiresByType
		// This lets *.cache.* and *.nocache.* work without needing to clear each type
		this.expiresByType = null;

		this.expiresDefault = expiresDefault;
	}

}
