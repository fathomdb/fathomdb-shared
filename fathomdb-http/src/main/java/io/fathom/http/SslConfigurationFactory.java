package io.fathom.http;

import javax.inject.Inject;

import com.fathomdb.Configuration;
import com.fathomdb.crypto.EncryptionStore;

public class SslConfigurationFactory {

	@Inject
	Configuration configuration;

	@Inject
	EncryptionStore encryptionStore;

	public SslConfiguration build(String prefix) {
		return SslConfiguration.fromConfiguration(encryptionStore, configuration, prefix);
	}

}
