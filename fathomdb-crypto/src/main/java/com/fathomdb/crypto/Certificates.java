package com.fathomdb.crypto;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.security.cert.X509Certificate;
import java.util.List;

import org.bouncycastle.openssl.PEMReader;

import com.fathomdb.crypto.bouncycastle.BouncyCastleLoader;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

public class Certificates {
    public static String getSubject(X509Certificate cert) {
        return cert.getSubjectX500Principal().getName();
    }

    public static boolean isSelfSigned(X509Certificate cert) {
        return cert.getSubjectDN().equals(cert.getIssuerDN());
    }

    public static List<X509Certificate> fromPem(String cert) {
        List<X509Certificate> certificates = Lists.newArrayList();

        PEMReader reader = null;
        try {
            reader = new PEMReader(new StringReader(cert), null, BouncyCastleLoader.getName());
            while (true) {
                Object o = reader.readObject();
                if (o == null) {
                    return certificates;
                }

                certificates.add((X509Certificate) o);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Error parsing certificate", e);
        } finally {
            Closeables.closeQuietly(reader);
        }
    }

    public static List<X509Certificate> fromPem(File path) throws IOException {
        return fromPem(Files.toString(path, Charsets.UTF_8));
    }

}
