package com.fathomdb.config;

public abstract class ConfigObject {
	final String version;

	protected ConfigObject(String versionKey) {
		this.version = versionKey;
	}

	public String getVersion() {
		return version;
	}

	public boolean isPresent() {
		return version != null;
	}
}
