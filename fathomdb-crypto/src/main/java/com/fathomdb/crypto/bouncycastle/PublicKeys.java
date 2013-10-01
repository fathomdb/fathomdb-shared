package com.fathomdb.crypto.bouncycastle;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.PublicKey;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;

import com.fathomdb.io.IoUtils;
import com.google.common.io.Closeables;

public class PublicKeys {
    // public static PublicKey fromEncoded(ByteSource byteSource) {
    // try {
    // KeyFactory kf = KeyFactory.getInstance("RSA");
    // X509EncodedKeySpec x = new X509EncodedKeySpec(byteSource.read());
    // // X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
    // // String algorithmOID=
    // // spki.getAlgorithmId().getObjectId().getId();
    // // String algorithm = OIDLookup.getCipherName(algorithmOID);
    // // if (algorithm == null) {
    // // throw new CertificateEncodingException("Unknown key algorithm!");
    // // }
    // // KeyFactory fact = KeyFactory.getInstance(algorithm);
    // // return fact.generatePublic(keySpec);
    //
    // PublicKey publicKey = kf.generatePublic(x);
    // return publicKey;
    // } catch (Exception e) {
    // throw new IllegalArgumentException("Error reading key", e);
    // }
    // }

    public static PublicKey fromPem(String data) {
        PEMReader reader = null;
        try {
            reader = new PEMReader(new StringReader(data), null, BouncyCastleLoader.getName());
            while (true) {
                Object o = reader.readObject();
                if (o instanceof PublicKey) {
                    return (PublicKey) o;
                } else if (o instanceof KeyPair) {
                    return ((KeyPair) o).getPublic();
                } else {
                    throw new IllegalArgumentException("Unexpected value found when looking for public key; found: "
                            + o.getClass());
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Error parsing public key", e);
        } finally {
            IoUtils.safeClose(reader);
        }
    }

    public static PublicKey fromPem(File path) throws IOException {
        return fromPem(IoUtils.readAll(path));
    }

    public static String toPem(PublicKey key) {
        PEMWriter pemWriter = null;
        try {
            StringWriter stringWriter = new StringWriter();
            pemWriter = new PEMWriter(stringWriter);

            pemWriter.writeObject(key);
            pemWriter.flush();

            return stringWriter.toString();
        } catch (IOException e) {
            throw new IllegalArgumentException("Error serializing key data", e);
        } finally {
            Closeables.closeQuietly(pemWriter);
        }
    }

}
