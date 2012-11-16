package com.fathomdb;

import java.io.File;
import java.util.Properties;

public interface Configuration {
	String get(String key);

	String lookup(String key, String defaultValue);

	int lookup(String key, int defaultValue);

	String find(String key);

	File getBasePath();

	File lookupFile(String key, String defaultValue);

	Properties getChildProperties(String prefix);
}
