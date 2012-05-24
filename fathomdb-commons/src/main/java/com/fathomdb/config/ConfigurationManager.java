package com.fathomdb.config;

import java.util.List;

import com.google.common.collect.Lists;

public class ConfigurationManager {
	public static final ConfigurationManager INSTANCE = new ConfigurationManager();

	final List<HasConfiguration> configurables = Lists.newArrayList();

	private ConfigurationManager() {
	}

	public void refresh() {
		for (HasConfiguration configurable : configurables) {
			configurable.refresh();
		}
	}

	public void register(HasConfiguration configurable) {
		configurables.add(configurable);
	}
}
