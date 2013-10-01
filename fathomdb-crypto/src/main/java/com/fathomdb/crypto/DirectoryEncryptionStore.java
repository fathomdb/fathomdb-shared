package com.fathomdb.crypto;

import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

import com.fathomdb.crypto.bouncycastle.PrivateKeys;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;

public class DirectoryEncryptionStore implements EncryptionStore {
    private final File base;

    public DirectoryEncryptionStore(File base) {
        this.base = base;
    }

    @Override
    public CertificateAndKey findCertificateAndKey(String alias) {
        CertificateAndKey certificateAndKey;

        Preconditions.checkNotNull(alias);

        // Path to file
        File certPath = new File(base, alias + ".crt");

        if (!certPath.exists()) {
            return null;
        }

        List<X509Certificate> certificate;
        try {
            certificate = Certificates.fromPem(certPath);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading certificate: " + certPath, e);
        }

        File keyPath = new File(base, alias + ".key");

        PrivateKey privateKey;
        try {
            privateKey = PrivateKeys.fromPem(keyPath);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading private key: " + keyPath, e);
        }

        certificateAndKey = new SimpleCertificateAndKey(certificate, privateKey);

        return certificateAndKey;
    }

    @Override
    public CertificateAndKey getCertificateAndKey(String cert) {
        CertificateAndKey certificateAndKey = findCertificateAndKey(cert);
        if (certificateAndKey == null) {
            throw new IllegalArgumentException("Certificate not found: " + cert);
        }
        return certificateAndKey;
    }

    @Override
    public void setCertificateAndKey(String alias, CertificateAndKey certificateAndKey) {
        Preconditions.checkNotNull(alias);

        File certPath = new File(base, alias + ".crt");
        File keyPath = new File(base, alias + ".key");

        if (certPath.exists() || keyPath.exists()) {
            throw new IllegalStateException("Alias already exists");
        }

        File dir = certPath.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String certificate = Certificates.toPem(certificateAndKey.getCertificateChain());
        String privateKey = PrivateKeys.toPem(certificateAndKey.getPrivateKey());

        try {
            Files.write(certificate, certPath, Charsets.UTF_8);
            Files.write(privateKey, keyPath, Charsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error writing certificate/key: " + alias, e);
        }
    }
}
