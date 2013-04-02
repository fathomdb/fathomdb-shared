package com.fathomdb.crypto.ssl;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultSslServerPolicy extends SslServerPolicy {
	private static final Logger log = LoggerFactory.getLogger(DefaultSslServerPolicy.class);

	private static final String PROTOCOL = "TLS";

	SSLContext sslContext;

	final KeyManager keyManager;
	final TrustManager trustManager;

	public DefaultSslServerPolicy(KeyManager keyManager, TrustManager trustManager) {
		super();
		this.keyManager = keyManager;
		this.trustManager = trustManager;
	}

	synchronized SSLContext getSslContext() {
		if (sslContext == null) {
			sslContext = buildSslContext();
		}
		return sslContext;
	}

	SSLContext buildSslContext() {
		try {
			SSLContext serverContext = SSLContext.getInstance(PROTOCOL);

			KeyManager[] keyManagers = null;
			if (keyManager != null) {
				keyManagers = new KeyManager[] { keyManager };
			}

			TrustManager[] trustManagers = null;

			if (trustManager != null) {
				trustManagers = new TrustManager[] { trustManager };
			}

			serverContext.init(keyManagers, trustManagers, new SecureRandom());

			return serverContext;
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Error building SSL engine", e);
		}
	}

	public SSLEngine createSSLEngine() {
		SSLContext sslContext = getSslContext();
		SSLEngine sslEngine = sslContext.createSSLEngine();

		SslPolicy.DEFAULT.applyPolicy(sslContext, sslEngine);

		return sslEngine;

	}
}