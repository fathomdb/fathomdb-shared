package com.fathomdb.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import sun.security.x509.CertAndKeyGen;
import sun.security.x509.X500Name;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

public class KeyStoreUtils {
	public static final String DEFAULT_KEYSTORE_SECRET = "notasecret";

	public static KeyStore load(File keystoreFile) throws KeyStoreException, IOException, NoSuchAlgorithmException,
			CertificateException {
		return load(keystoreFile, DEFAULT_KEYSTORE_SECRET);
	}

	public static KeyStore load(File keystoreFile, String keystoreSecret) throws KeyStoreException, IOException,
			NoSuchAlgorithmException, CertificateException {
		InputStream is = null;

		try {
			is = new FileInputStream(keystoreFile);
			return load(is, keystoreSecret);
		} finally {
			Closeables.closeQuietly(is);
		}
	}

	public static KeyStore load(byte[] data, String keystoreSecret) throws KeyStoreException, IOException,
			NoSuchAlgorithmException, CertificateException {
		InputStream is = null;

		try {
			is = new ByteArrayInputStream(data);
			return load(is, keystoreSecret);
		} finally {
			Closeables.closeQuietly(is);
		}
	}

	public static KeyStore load(InputStream is, String keystoreSecret) throws KeyStoreException, IOException,
			NoSuchAlgorithmException, CertificateException {
		KeyStore keystore = create();
		keystore.load(is, keystoreSecret.toCharArray());

		return keystore;
	}

	public static List<String> getAliases(KeyStore keystore) throws KeyStoreException {
		List<String> ret = Lists.newArrayList();

		Enumeration<String> aliases = keystore.aliases();
		while (aliases.hasMoreElements()) {
			String alias = aliases.nextElement();
			ret.add(alias);
		}

		return ret;
	}

	private static KeyStore create() {
		KeyStore keystore;
		try {
			keystore = KeyStore.getInstance("JKS");
		} catch (KeyStoreException e) {
			throw new IllegalStateException("Error building keystore", e);
		}
		return keystore;
	}

	public static KeyStore createEmpty(String keystoreSecret) throws KeyStoreException, NoSuchAlgorithmException,
			CertificateException, IOException {
		return load((InputStream) null, keystoreSecret);
	}

	public static List<Certificate[]> getCertificateChains(KeyStore keystore) throws KeyStoreException {
		List<Certificate[]> chains = Lists.newArrayList();

		for (String alias : getAliases(keystore)) {
			Certificate[] certificateChain = keystore.getCertificateChain(alias);
			if (certificateChain == null) {
				continue;
			}
			chains.add(certificateChain);
		}
		return chains;
	}

	public static List<String> getKeyAliases(KeyStore keystore) throws KeyStoreException {
		List<String> ret = Lists.newArrayList();

		for (String alias : getAliases(keystore)) {
			if (keystore.isKeyEntry(alias)) {
				ret.add(alias);
			}
		}

		return ret;
	}

	/*
	 * Based on how keytool does it
	 */
	public static void createSelfSigned(KeyStore keystore, String alias, String keyPassword, X500Name x500Name,
			int validityDays, String keyAlgorithmName, int keySize, String signatureAlgName)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, CertificateException,
			SignatureException, KeyStoreException {

		String providerName = null;
		CertAndKeyGen keypair = new CertAndKeyGen(keyAlgorithmName, signatureAlgName, providerName);

		keypair.generate(keySize);
		PrivateKey privKey = keypair.getPrivateKey();

		X509Certificate[] chain = new X509Certificate[1];

		Date startDate = new Date(System.currentTimeMillis() - 24L * 60L * 60L);
		chain[0] = keypair.getSelfCertificate(x500Name, startDate, (validityDays + 1) * 24L * 60L * 60L);

		keystore.setKeyEntry(alias, privKey, keyPassword.toCharArray(), chain);
	}

	public static void createSelfSigned(KeyStore keystore, String alias, String keyPassword, X500Name x500Name,
			int validity) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException,
			CertificateException, SignatureException, KeyStoreException {
		createSelfSigned(keystore, alias, keyPassword, x500Name, validity, "RSA", 2048, "SHA1WithRSA");
	}

	public static byte[] serialize(KeyStore keystore, String keystoreSecret) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			keystore.store(baos, keystoreSecret.toCharArray());
			return baos.toByteArray();
		} finally {
			Closeables.closeQuietly(baos);
		}
	}

	public static CertificateAndKey getCertificateAndKey(KeyStore keyStore, String alias, String password)
			throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException {
		if (!keyStore.isKeyEntry(alias)) {
			return null;
		}

		ProtectionParameter protParam = new KeyStore.PasswordProtection(password.toCharArray());
		Entry key = keyStore.getEntry(alias, protParam);
		if (key == null || !(key instanceof PrivateKeyEntry)) {
			return null;
		}

		return new KeystoreCertificateAndKey((PrivateKeyEntry) key);
	}

	public static void put(KeyStore keystore, String alias, CertificateAndKey certificateAndKey, String secret)
			throws GeneralSecurityException {
		keystore.setKeyEntry(alias, certificateAndKey.getPrivateKey(), secret.toCharArray(),
				certificateAndKey.getCertificateChain());
	}
}
