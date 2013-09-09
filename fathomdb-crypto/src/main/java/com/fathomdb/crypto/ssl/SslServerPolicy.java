package com.fathomdb.crypto.ssl;

import javax.net.ssl.SSLEngine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SslServerPolicy {
    private static final Logger log = LoggerFactory.getLogger(SslServerPolicy.class);

    public abstract SSLEngine createSSLEngine();

    public boolean isSslEnabled() {
        return true;
    }

}