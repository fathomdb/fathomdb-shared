package com.fathomdb.crypto;

public class NullEncryptionStore implements EncryptionStore {

    @Override
    public CertificateAndKey getCertificateAndKey(String cert) {
        throw new IllegalArgumentException("No certificates configured (NullEncryptionStore)");
    }

}
