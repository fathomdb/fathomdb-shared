package com.fathomdb.crypto;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleClientCertificateKeyManager extends X509ExtendedKeyManager {
	private static final Logger log = LoggerFactory.getLogger(SimpleClientCertificateKeyManager.class);

	private static final String ALIAS = "cert";

	final PrivateKey privateKey;
	final X509Certificate[] certificateChain;

	public SimpleClientCertificateKeyManager(PrivateKey privateKey, X509Certificate[] certificateChain) {
		super();
		this.privateKey = privateKey;
		this.certificateChain = certificateChain;
	}

	public SimpleClientCertificateKeyManager(CertificateAndKey certificateAndKey) {
		this(certificateAndKey.getPrivateKey(), certificateAndKey.getCertificateChain());
	}

	@Override
	public String[] getClientAliases(String keyType, Principal[] issuers) {
		log.warn("getClientAliases not supported");
		throw new UnsupportedOperationException();
	}

	@Override
	public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
		// log.debug("Using client alias: " + ALIAS);
		return ALIAS;
	}

	@Override
	public String[] getServerAliases(String keyType, Principal[] issuers) {
		log.warn("getServerAliases not implemented");

		throw new UnsupportedOperationException();
	}

	@Override
	public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
		log.warn("chooseServerAlias not implemented");

		throw new UnsupportedOperationException();
	}

	@Override
	public X509Certificate[] getCertificateChain(String alias) {
		if (!alias.equals(ALIAS)) {
			log.warn("Alias mismatch: " + alias);

			throw new IllegalArgumentException();
		}

		// log.info("Using client certificate: " + alias + " length=" + certificateChain.length);
		// log.debug("Client certificate = " + Joiner.on("\n").join(certificateChain));

		return certificateChain;
	}

	@Override
	public PrivateKey getPrivateKey(String alias) {
		if (!alias.equals(ALIAS)) {
			log.warn("Alias mismatch: " + alias);

			throw new IllegalArgumentException();
		}

		// log.info("Using private key: " + alias);

		return privateKey;
	}

	@Override
	public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
		// log.debug("Using client alias (chooseEngineClientAlias): " + ALIAS);
		return ALIAS;
	}

	@Override
	public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
		log.warn("chooseEngineServerAlias not implemented");

		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "SimpleClientCertificateKeyManager [certificateChain=" + Arrays.toString(certificateChain) + "]";
	}
}
