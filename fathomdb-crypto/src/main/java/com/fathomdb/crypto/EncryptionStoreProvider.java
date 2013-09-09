package com.fathomdb.crypto;

import java.io.File;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.Configuration;

public class EncryptionStoreProvider implements javax.inject.Provider<EncryptionStore> {
    private static final Logger log = LoggerFactory.getLogger(EncryptionStoreProvider.class);

    @Inject
    Configuration configuration;

    @Override
    public EncryptionStore get() {
        return build(configuration);
    }

    public static EncryptionStore build(Configuration configuration) {
        File keystoreFile = configuration.lookupFile("keystore", null);

        if (keystoreFile != null && keystoreFile.isDirectory()) {
            return new DirectoryEncryptionStore(keystoreFile);
        }

        return new NullEncryptionStore();
        //
        // if (keystoreFile == null) {
        // keystoreFile = new File(configuration.getBasePath(), "keystore.jks");
        // if (!keystoreFile.exists()) {
        // log.warn("No keystore specified (or found); starting with an empty keystore");
        //
        // try {
        // KeyStore keyStore =
        // KeyStoreUtils.createEmpty(KeyStoreUtils.DEFAULT_KEYSTORE_SECRET);
        // return new KeyStoreEncryptionStore(keyStore);
        // } catch (GeneralSecurityException e) {
        // throw new IllegalStateException("Error creating keystore", e);
        // } catch (IOException e) {
        // throw new IllegalStateException("Error creating keystore", e);
        // }
        // }
        // }
        //
        // String secret = configuration.lookup("keystore.password",
        // KeyStoreUtils.DEFAULT_KEYSTORE_SECRET);
        // return KeyStoreEncryptionStore.build(keystoreFile, secret);
    }

}
