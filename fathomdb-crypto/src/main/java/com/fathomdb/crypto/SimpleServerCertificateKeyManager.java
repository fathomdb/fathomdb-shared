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

public class SimpleServerCertificateKeyManager extends X509ExtendedKeyManager {
	private static final Logger log = LoggerFactory.getLogger(SimpleServerCertificateKeyManager.class);

	private static final String ALIAS = "cert";

	final PrivateKey privateKey;
	final X509Certificate[] certificateChain;

	public SimpleServerCertificateKeyManager(PrivateKey privateKey, X509Certificate[] certificateChain) {
		super();
		this.privateKey = privateKey;
		this.certificateChain = certificateChain;
	}

	public SimpleServerCertificateKeyManager(CertificateAndKey certificateAndKey) {
		this(certificateAndKey.getPrivateKey(), certificateAndKey.getCertificateChain());
	}

	@Override
	public String[] getClientAliases(String keyType, Principal[] issuers) {
		log.warn("getClientAliases not supported");
		throw new UnsupportedOperationException();
	}

	@Override
	public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
		log.warn("chooseClientAlias not supported");
		throw new UnsupportedOperationException();
	}

	@Override
	public String[] getServerAliases(String keyType, Principal[] issuers) {
		return new String[] { ALIAS };
	}

	@Override
	public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
		return ALIAS;
	}

	@Override
	public X509Certificate[] getCertificateChain(String alias) {
		if (!alias.equals(ALIAS)) {
			log.warn("Alias mismatch: " + alias);

			throw new IllegalArgumentException();
		}

		// log.info("Using server certificate: " + alias + " length=" + certificateChain.length);
		// log.debug("Server certificate = " + Joiner.on("\n").join(certificateChain));

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
		log.warn("chooseEngineClientAlias not implemented");

		throw new UnsupportedOperationException();
	}

	@Override
	public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
		// log.debug("Using client alias (chooseEngineServerAlias): " + ALIAS);
		return ALIAS;
	}

	@Override
	public String toString() {
		return "SimpleServerCertificateKeyManager [certificateChain=" + Arrays.toString(certificateChain) + "]";
	}
}
