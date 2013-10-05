package io.fathom.http;

import java.io.Closeable;
import java.net.URI;

public interface HttpClient extends Closeable {
    HttpRequest buildRequest(HttpMethod method, URI uri);

    HttpClient withSsl(SslConfiguration sslConfiguration);

    SslConfiguration getSslConfiguration();
}
