package io.fathom.rest;

import io.fathom.http.HttpMethod;

import java.io.PrintStream;
import java.net.URI;

public interface RestfulClient {
	<T> RestfulRequest<T> buildRequest(HttpMethod method, String relativeUri, HttpEntity postObject,
			Class<T> responseClass);

	URI getBaseUri();

	void setDebug(PrintStream debug);
}
