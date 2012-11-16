package org.platformlayer.http.jre;

import org.platformlayer.http.HttpConfiguration;
import org.platformlayer.http.HttpStrategy;
import org.platformlayer.http.SslConfiguration;

public class JreHttpStrategy implements HttpStrategy {

	@Override
	public HttpConfiguration buildConfiguration(SslConfiguration sslConfiguration) {
		return new JreHttpConfiguration(sslConfiguration);
	}

}
