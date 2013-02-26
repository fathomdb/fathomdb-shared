package com.fathomdb.crypto.ssl;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class SslPolicy {

	private static final Logger log = LoggerFactory.getLogger(SslPolicy.class);

	// Due to a bug in Java 7, we temporarily need to block ECHDE
	// http://shickys.blogspot.com/
	// https://bugs.launchpad.net/ubuntu/+source/openjdk-7/+bug/989240
	// http://stackoverflow.com/questions/10687200/java-7-and-could-not-generate-dh-keypair/11688235#11688235
	private static final boolean BLOCK_ECHDE = true;

	private final String[] enabledCipherSuites;
	private final String[] enabledProtocols;

	public static final SslPolicy DEFAULT = new SslPolicy();

	private SslPolicy() {
		List<String> enabledCipherSuites;
		List<String> enabledProtocols;

		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, null, null);
			SSLEngine engine = sslContext.createSSLEngine();

			enabledCipherSuites = Lists.newArrayList(engine.getEnabledCipherSuites());
			enabledProtocols = Lists.newArrayList(engine.getEnabledProtocols());
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("Cannot build SSL Context", e);
		} catch (KeyManagementException e) {
			throw new IllegalStateException("Cannot build SSL Context", e);
		}

		if (BLOCK_ECHDE) {
			log.warn("Blocking elliptic curve cipher suites due to bug in OpenJDK crypto");
		}

		Iterator<String> it = enabledCipherSuites.iterator();
		while (it.hasNext()) {
			String cipher = it.next();

			boolean remove = false;
			if (BLOCK_ECHDE && cipher.contains("_ECDHE_")) {
				remove = true;
			}

			if (remove) {
				it.remove();
			}
		}

		// Google's List, from https://www.ssllabs.com/ssltest/index.html
		// Cipher Suites (SSLv3+ suites in server-preferred order, then SSLv2 suites where used)
		// TLS_ECDHE_RSA_WITH_RC4_128_SHA (0xc011) ECDH 256 bits (eq. 3072 bits RSA) 128
		// TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA (0xc013) ECDH 256 bits (eq. 3072 bits RSA) 128
		// TLS_RSA_WITH_RC4_128_SHA (0x5) 128
		// TLS_RSA_WITH_RC4_128_MD5 (0x4) 128
		// TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384 (0xc030) ECDH 256 bits (eq. 3072 bits RSA) 256
		// TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384 (0xc028) ECDH 256 bits (eq. 3072 bits RSA) 256
		// TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA (0xc014) ECDH 256 bits (eq. 3072 bits RSA) 256
		// TLS_RSA_WITH_AES_256_GCM_SHA384 (0x9d) 256
		// TLS_RSA_WITH_AES_256_CBC_SHA256 (0x3d) 256
		// TLS_RSA_WITH_AES_256_CBC_SHA (0x35) 256
		// TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA (0xc012) ECDH 256 bits (eq. 3072 bits RSA) 168
		// TLS_RSA_WITH_3DES_EDE_CBC_SHA (0xa) 168
		// TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256 (0xc02f) ECDH 256 bits (eq. 3072 bits RSA) 128
		// TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256 (0xc027) ECDH 256 bits (eq. 3072 bits RSA) 128
		// TLS_RSA_WITH_AES_128_GCM_SHA256 (0x9c) 128
		// TLS_RSA_WITH_AES_128_CBC_SHA256 (0x3c) 128
		// TLS_RSA_WITH_AES_128_CBC_SHA (0x2f) 128

		log.debug("Enabled ciphers:");
		for (String cipher : enabledCipherSuites) {
			log.debug("\t" + cipher);
		}

		this.enabledCipherSuites = enabledCipherSuites.toArray(new String[enabledCipherSuites.size()]);

		log.debug("Enabled protocols:");
		for (String enabledProtocol : enabledProtocols) {
			log.debug("\t" + enabledProtocol);
		}
		this.enabledProtocols = enabledProtocols.toArray(new String[enabledProtocols.size()]);

		//
		// engine.setEnabledCipherSuites(new String[] {
		// // "TLS_ECDHE_RSA_WITH_RC4_128_SHA", // (0xc011) ECDH 256 bits (eq. 3072 bits RSA) 128
		// // "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", // (0xc013) ECDH 256 bits (eq. 3072 bits RSA) 128
		// // NOT_JAVA "TLS_RSA_WITH_RC4_128_SHA", // (0x5) 128
		// // NOT_JAVA "TLS_RSA_WITH_RC4_128_MD5", // (0x4) 128
		// // NOT_JAVA "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", // (0xc030) ECDH 256 bits (eq. 3072 bits RSA)
		// // 256
		// // "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384", // (0xc028) ECDH 256 bits (eq. 3072 bits RSA) 256
		// // "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", // (0xc014) ECDH 256 bits (eq. 3072 bits RSA) 256
		// // NOT_JAVA "TLS_RSA_WITH_AES_256_GCM_SHA384", // (0x9d) 256
		// "TLS_RSA_WITH_AES_256_CBC_SHA256", // (0x3d) 256
		// "TLS_RSA_WITH_AES_256_CBC_SHA", // (0x35) 256
		// // NOT_JAVA "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA", // (0xc012) ECDH 256 bits (eq. 3072 bits RSA) 168
		// // NOT_JAVA "TLS_RSA_WITH_3DES_EDE_CBC_SHA", // (0xa) 168
		// // NOT_JAVA "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", // (0xc02f) ECDH 256 bits (eq. 3072 bits RSA)
		// // 128
		// // "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", // (0xc027) ECDH 256 bits (eq. 3072 bits RSA) 128
		// // NOT_JAVA "TLS_RSA_WITH_AES_128_GCM_SHA256", // (0x9c) 128
		// "TLS_RSA_WITH_AES_128_CBC_SHA256", // (0x3c) 128
		// "TLS_RSA_WITH_AES_128_CBC_SHA", // (0x2f) 128
		// });
	}

	public String[] getEnabledCipherSuites() {
		return enabledCipherSuites;
	}

	public String[] getEnabledProtocols() {
		return enabledProtocols;
	}

	public void applyPolicy(SSLEngine engine) {
		engine.setEnabledCipherSuites(getEnabledCipherSuites());
		engine.setEnabledProtocols(getEnabledProtocols());
	}

}
