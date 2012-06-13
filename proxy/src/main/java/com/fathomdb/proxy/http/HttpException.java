package com.fathomdb.proxy.http;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public class HttpException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public static final HttpException NOT_FOUND = new HttpException(HttpResponseStatus.NOT_FOUND, false);
	public static final HttpException METHOD_NOT_ALLOWED = new HttpException(HttpResponseStatus.METHOD_NOT_ALLOWED,
			false);

	private final HttpResponseStatus status;
	private final boolean hasStackTrace;

	protected HttpException(HttpResponseStatus status, boolean hasStackTrace) {
		this.status = status;
		this.hasStackTrace = hasStackTrace;
	}

	public HttpResponseStatus getStatus() {
		return status;
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		if (!hasStackTrace) {
			return this;
		}
		return super.fillInStackTrace();
	}

}
