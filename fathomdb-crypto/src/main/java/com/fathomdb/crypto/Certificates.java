package com.fathomdb.crypto;

import java.security.cert.X509Certificate;

public class Certificates {
	public static String getSubject(X509Certificate cert) {
		return cert.getSubjectX500Principal().getName();
	}

	public static boolean isSelfSigned(X509Certificate cert) {
		return cert.getSubjectDN().equals(cert.getIssuerDN());
	}

}
