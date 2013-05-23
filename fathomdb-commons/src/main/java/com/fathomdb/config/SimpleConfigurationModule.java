package com.fathomdb.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.Configuration;
import com.google.inject.AbstractModule;

public class SimpleConfigurationModule extends AbstractModule {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(SimpleConfigurationModule.class);

	private final ConfigurationImpl configuration;

	public SimpleConfigurationModule(ConfigurationImpl configuration) {
		this.configuration = configuration;
	}

	public SimpleConfigurationModule() {
		this(ConfigurationImpl.load());
	}

	@Override
	protected void configure() {
		bind(Configuration.class).toInstance(configuration);
	}

	public ConfigurationImpl getConfiguration() {
		return configuration;
	}
}
