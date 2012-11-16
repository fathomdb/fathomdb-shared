package com.fathomdb.crypto;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;


public class SimpleCertificateAndKey implements CertificateAndKey {

	final X509Certificate[] chain;
	final PrivateKey privateKey;

	public SimpleCertificateAndKey(List<X509Certificate> chain, PrivateKey privateKey) {
		this(chain.toArray(new X509Certificate[chain.size()]), privateKey);
	}

	public SimpleCertificateAndKey(X509Certificate[] chain, PrivateKey privateKey) {
		super();
		this.chain = chain;
		this.privateKey = privateKey;
	}

	@Override
	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	@Override
	public X509Certificate[] getCertificateChain() {
		return chain;
	}

	@Override
	public PublicKey getPublicKey() {
		return chain[0].getPublicKey();
	}
}