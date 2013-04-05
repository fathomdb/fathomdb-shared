package org.platformlayer.rest;

import java.io.PrintStream;
import java.net.URI;

import org.platformlayer.http.HttpMethod;

public interface RestfulClient {
	<T> RestfulRequest<T> buildRequest(HttpMethod method, String relativeUri, HttpPayload postObject,
			Class<T> responseClass);

	URI getBaseUri();

	void setDebug(PrintStream debug);
}
