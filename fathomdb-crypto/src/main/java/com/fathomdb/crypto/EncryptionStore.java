package com.fathomdb.crypto;

public interface EncryptionStore {
	CertificateAndKey getCertificateAndKey(String cert);
}
