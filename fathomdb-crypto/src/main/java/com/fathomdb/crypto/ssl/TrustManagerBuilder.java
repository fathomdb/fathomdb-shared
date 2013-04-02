package com.fathomdb.crypto.ssl;

import java.security.PublicKey;
import java.util.List;

import javax.net.ssl.TrustManager;

import com.fathomdb.crypto.OpenSshUtils;
import com.google.common.collect.Lists;

public class TrustManagerBuilder {

	final List<String> publicKeys = Lists.newArrayList();

	public TrustManager build() {
		return new PublicKeyTrustManager(publicKeys);
	}

	public void addTrustedKey(PublicKey publicKey) {
		String sigString = OpenSshUtils.getSignatureString(publicKey);
		publicKeys.add(sigString);
	}

	public void addTrustedKeySignature(String sigString) {
		publicKeys.add(sigString);
	}

}
