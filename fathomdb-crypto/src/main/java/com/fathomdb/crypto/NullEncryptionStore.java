package com.fathomdb.crypto;

public class NullEncryptionStore implements EncryptionStore {

    @Override
    public CertificateAndKey getCertificateAndKey(String cert) {
        throw new IllegalArgumentException("No certificates configured (NullEncryptionStore)");
    }

    @Override
    public CertificateAndKey findCertificateAndKey(String cert) {
        return null;
    }

    @Override
    public void setCertificateAndKey(String alias, CertificateAndKey certificateAndKey) {
        throw new UnsupportedOperationException();
    }

}
