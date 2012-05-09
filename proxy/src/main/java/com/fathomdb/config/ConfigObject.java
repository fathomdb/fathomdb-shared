package com.fathomdb.config;

public class ConfigObject {
	final String key;
	final String version;

	protected ConfigObject(String key, String versionKey) {
		this.key = key;
		this.version = versionKey;
	}

	public String getKey() {
		return key;
	}

	public String getVersion() {
		return version;
	}
}
