package io.fathom.rest;

import io.fathom.http.SslConfiguration;

import javax.net.ssl.KeyManager;

public interface RestfulRequest<T> {

	T execute() throws RestClientException;

	SslConfiguration getSslConfiguration();

	void setKeyManager(KeyManager keyManager);

}
