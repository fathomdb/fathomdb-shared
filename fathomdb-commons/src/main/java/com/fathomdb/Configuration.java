package com.fathomdb;

import java.io.File;
import java.util.Map;

public interface Configuration {
	String get(String key);

	String lookup(String key, String defaultValue);

	boolean lookup(String key, boolean defaultValue);

	int lookup(String key, int defaultValue);

	String find(String key);

	File getBasePath();

	File lookupFile(String key, String defaultValue);

	Map<String, String> getChildProperties(String prefix);
}
