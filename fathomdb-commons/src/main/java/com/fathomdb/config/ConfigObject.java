package com.fathomdb.config;

public abstract class ConfigObject {
	final String version;

	protected ConfigObject(String versionKey) {
		this.version = versionKey;
	}

	public abstract boolean isPresent();

	public String getVersion() {
		return version;
	}
}
