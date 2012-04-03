package com.fathomdb.proxy.http.config;

import java.util.List;

import com.google.common.collect.Lists;

public class Configuration {
	public static final Configuration INSTANCE = new Configuration();

	final List<HasConfiguration> configurables = Lists.newArrayList();

	private Configuration() {
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
