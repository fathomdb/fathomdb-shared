package io.fathom.http;


public interface HttpStrategy {
	HttpClient buildConfiguration(SslConfiguration sslConfiguration);
}
