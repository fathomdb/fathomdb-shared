package org.platformlayer.http;

public enum HttpMethod {
	GET, POST, PUT, DELETE;

	public String getHttpMethod() {
		return name();
	}
}
