package com.fathomdb.proxy.htaccess;

import com.fathomdb.proxy.http.handlers.ContentType;

public class ExpiresByType extends ExpirationDirective {
	final ContentType contentType;

	ExpiresByType(ParseDirectiveNode node, ExpirationTimeout timeout, ContentType contentType) {
		super(node, timeout);
		this.contentType = contentType;
	}

	public static ExpiresByType parse(ParseDirectiveNode node) {
		ContentType contentType;
		ExpirationTimeout timeout;

		try {
			HtaccessTokenizer tokenizer = new HtaccessTokenizer(node.arguments);

			String contentTypeString = tokenizer.pop();
			contentType = ContentType.get(contentTypeString);

			String expiration = tokenizer.pop();

			timeout = ExpirationTimeout.parse(expiration);
		} catch (Exception e) {
			throw new IllegalArgumentException("Error parsing directive", e);
		}

		return new ExpiresByType(node, timeout, contentType);
	}

	@Override
	public String toString() {
		return "ExpiresByType [contentType=" + contentType + super.toStringHelper() + "]";
	}

	public ContentType getContentType() {
		return contentType;
	}

}
