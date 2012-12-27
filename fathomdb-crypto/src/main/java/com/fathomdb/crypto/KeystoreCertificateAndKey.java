package com.fathomdb.crypto;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;


public class KeystoreCertificateAndKey implements CertificateAndKey {
	final PrivateKeyEntry privateKeyEntry;

	public KeystoreCertificateAndKey(PrivateKeyEntry privateKeyEntry) {
		super();
		this.privateKeyEntry = privateKeyEntry;
	}

	@Override
	public PrivateKey getPrivateKey() {
		return privateKeyEntry.getPrivateKey();
	}

	@Override
	public X509Certificate[] getCertificateChain() {
		Certificate[] chain = privateKeyEntry.getCertificateChain();

		X509Certificate[] x509 = new X509Certificate[chain.length];

		for (int i = 0; i < chain.length; i++) {
			x509[i] = (X509Certificate) chain[i];
		}

		return x509;
	}

	@Override
	public PublicKey getPublicKey() {
		Certificate[] chain = privateKeyEntry.getCertificateChain();
		return chain[0].getPublicKey();
	}
}
