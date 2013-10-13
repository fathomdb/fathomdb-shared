package io.fathom.http.jre;

import io.fathom.http.HttpClient;
import io.fathom.http.HttpStrategy;
import io.fathom.http.SslConfiguration;

public class JreHttpStrategy implements HttpStrategy {

    @Override
    public HttpClient buildConfiguration(SslConfiguration sslConfiguration) {
        return new JreHttpClient(sslConfiguration);
    }

}
