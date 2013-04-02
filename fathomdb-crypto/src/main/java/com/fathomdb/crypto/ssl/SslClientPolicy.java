package com.fathomdb.crypto.ssl;

import java.security.GeneralSecurityException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

public class SslClientPolicy {
	final TrustManager[] trustManagers;
	final KeyManager[] keyManagers;

	public SslClientPolicy(KeyManager[] keyManagers, TrustManager[] trustManagers) {
		super();
		this.trustManagers = trustManagers;
		this.keyManagers = keyManagers;
	}

	public SSLEngine createEngine() throws GeneralSecurityException {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		sslContext.init(keyManagers, trustManagers, null);

		SSLEngine sslEngine = sslContext.createSSLEngine();

		// SSLParameters sslParams = new SSLParameters();
		// sslParams.setEndpointIdentificationAlgorithm("HTTPS");
		// sslEngine.setSSLParameters(sslParams);

		// SslPolicy.DEFAULT.applyPolicy(sslEngine);

		return sslEngine;
	}

	public void verifySession(SSLSession session) {
		// BrowserCompatHostnameVerifier hostnameVerifier = new BrowserCompatHostnameVerifier();
		// if (!hostnameVerifier.verify(hostname, session)) {
		// throw new SecurityException("Peer verification failed for hostname: " + hostname);
		// }
	}

}
