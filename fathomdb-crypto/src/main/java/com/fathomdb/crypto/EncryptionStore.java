package com.fathomdb.crypto;

public interface EncryptionStore {
    CertificateAndKey getCertificateAndKey(String cert);

    CertificateAndKey findCertificateAndKey(String cert);

    void setCertificateAndKey(String alias, CertificateAndKey certificateAndKey);
}
