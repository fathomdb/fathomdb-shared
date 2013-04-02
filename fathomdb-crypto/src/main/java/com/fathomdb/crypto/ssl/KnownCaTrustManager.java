package com.fathomdb.crypto.ssl;

import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.crypto.OpenSshUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class KnownCaTrustManager implements X509TrustManager {
	private static final Logger log = LoggerFactory.getLogger(KnownCaTrustManager.class);

	private final List<String[]> trustedServerCas;
	private final List<String[]> trustedClientCas;

	public KnownCaTrustManager(Iterable<String> serverCas, Iterable<String> clientCas) {
		this.trustedServerCas = parse(serverCas);
		this.trustedClientCas = parse(clientCas);
	}

	private static List<String[]> parse(Iterable<String> cas) {
		List<String[]> trusted = Lists.newArrayList();

		if (cas != null) {
			for (String keyspec : cas) {
				String[] tokens = keyspec.split("/");
				if (tokens.length == 0) {
					throw new IllegalArgumentException();
				}
				trusted.add(tokens);
			}
		}

		return trusted;

	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		if (chain.length == 0) {
			throw new IllegalArgumentException("null or zero-length certificate chain");
		}

		boolean found = matchesCa(trustedClientCas, chain);

		if (!found) {
			throw new CertificateException("Certificate is not trusted");
		}
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		if (chain.length == 0) {
			throw new IllegalArgumentException("null or zero-length certificate chain");
		}

		boolean found = matchesCa(trustedServerCas, chain);

		if (!found) {
			throw new CertificateException("Certificate is not trusted");
		}
	}

	static boolean matchesCa(List<String[]> trustedCas, X509Certificate[] chain) {
		boolean found = false;

		String[] chainSignatures = new String[chain.length];
		for (String[] trusted : trustedCas) {
			if (chainSignatures.length < trusted.length) {
				continue;
			}

			boolean match = true;
			for (int i = 0; i < trusted.length; i++) {
				String sig = chainSignatures[chain.length - 1 - i];
				if (sig == null) {
					PublicKey certPublicKey = chain[chain.length - 1 - i].getPublicKey();
					sig = OpenSshUtils.getSignatureString(certPublicKey);
					chainSignatures[chain.length - 1 - i] = sig;
				}

				if (!trusted[trusted.length - i - 1].equals(sig)) {
					match = false;
					break;
				}
			}

			if (match) {
				found = true;
				break;
			}
		}

		if (!found) {
			log.warn("Certificate is not trusted (" + Joiner.on(",").join(chainSignatures) + ")");
		}

		return found;
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[0];
	}

	public static String encode(X509Certificate[] chain) {
		List<String> sigs = Lists.newArrayList();

		for (int i = 0; i < chain.length; i++) {
			X509Certificate cert = chain[i];
			PublicKey certPublicKey = cert.getPublicKey();
			sigs.add(OpenSshUtils.getSignatureString(certPublicKey));
		}

		return Joiner.on('/').join(sigs);
	}

}
