package com.fathomdb.crypto;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public interface CertificateAndKey {

	PrivateKey getPrivateKey();

	X509Certificate[] getCertificateChain();

	PublicKey getPublicKey();
}
