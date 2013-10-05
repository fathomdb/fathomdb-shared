package io.fathom.http.jre;

import io.fathom.http.HttpClient;
import io.fathom.http.HttpMethod;
import io.fathom.http.HttpRequest;
import io.fathom.http.SslConfiguration;

import java.io.IOException;
import java.net.URI;

public class JreHttpConfiguration implements HttpClient {

    final SslConfiguration sslConfiguration;

    JreHttpConfiguration(SslConfiguration sslConfiguration) {
        super();
        if (sslConfiguration == null) {
            sslConfiguration = SslConfiguration.EMPTY;
        }
        this.sslConfiguration = sslConfiguration;
    }

    @Override
    public HttpRequest buildRequest(HttpMethod method, URI uri) {
        try {
            return new JreHttpRequest(method, uri, sslConfiguration);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error building http request", e);
        }
    }

    @Override
    public void close() throws IOException {
    }

    public static HttpClient create() {
        return new JreHttpConfiguration(SslConfiguration.EMPTY);
    }

    @Override
    public HttpClient withSsl(SslConfiguration sslConfiguration) {
        return new JreHttpConfiguration(sslConfiguration);
    }

    @Override
    public SslConfiguration getSslConfiguration() {
        return sslConfiguration;
    }

}
