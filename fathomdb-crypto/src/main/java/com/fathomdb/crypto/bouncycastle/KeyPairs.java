package com.fathomdb.crypto.bouncycastle;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyPair;

import org.bouncycastle.openssl.PEMReader;

import com.fathomdb.io.IoUtils;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class KeyPairs {
    public static KeyPair fromPem(String data) {
        PEMReader reader = null;
        try {
            reader = new PEMReader(new StringReader(data), null, BouncyCastleLoader.getName());
            while (true) {
                Object o = reader.readObject();
                if (o instanceof KeyPair) {
                    return ((KeyPair) o);
                } else {
                    throw new IllegalArgumentException("Unexpected value found when looking for key pair; found: "
                            + o.getClass());
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Error parsing key pair", e);
        } finally {
            IoUtils.safeClose(reader);
        }
    }

    public static KeyPair fromPem(File path) throws IOException {
        return fromPem(Files.toString(path, Charsets.UTF_8));
    }

}
