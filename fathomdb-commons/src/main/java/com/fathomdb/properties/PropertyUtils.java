package com.fathomdb.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;

public class PropertyUtils {
	public static Properties loadProperties(File file) throws IOException {
		Properties properties = new Properties();
		loadProperties(properties, file);
		return properties;
	}

	public static void loadProperties(Properties properties, File file) throws IOException {
		FileInputStream is = new FileInputStream(file);
		try {
			properties.load(is);
		} finally {
			Closeables.closeQuietly(is);
		}
	}

	public static <K, V> Map<String, V> getChildProperties(Map<K, V> base, String prefix) {
		Map<String, V> children = Maps.newHashMap();

		for (Map.Entry<K, V> entry : base.entrySet()) {
			Object keyObject = entry.getKey();
			if (!(keyObject instanceof String)) {
				continue;
			}

			String key = (String) keyObject;
			if (!key.startsWith(prefix)) {
				continue;
			}

			String suffix = key.substring(prefix.length());
			children.put(suffix, entry.getValue());
		}

		return children;
	}

	public static void copyToMap(Properties properties, Map<String, String> dest) {
		for (Entry<Object, Object> entry : properties.entrySet()) {
			dest.put((String) entry.getKey(), (String) entry.getValue());
		}
	}

	public static String serialize(Properties properties) throws IOException {
		boolean useStandardSerialization = false;
		if (useStandardSerialization) {
			StringWriter writer = new StringWriter();
			properties.store(writer, null);

			// The properties serialization normally puts a comment at the top with the date
			// That causes lots of false-positive changes; remove it
			return stripComments(writer.toString());
		} else {
			TreeMap<String, String> map = Maps.newTreeMap();
			for (Entry<Object, Object> entry : properties.entrySet()) {
				map.put((String) entry.getKey(), (String) entry.getValue());
			}

			StringWriter writer = new StringWriter();
			for (Entry<String, String> entry : map.entrySet()) {
				// TODO: Escaping??
				writer.write(entry.getKey());
				writer.write('=');
				writer.write(entry.getValue());
				writer.write('\n');
			}

			return writer.toString();
		}
	}

	private static String stripComments(String s) {
		StringBuilder sb = new StringBuilder();
		for (String line : Splitter.on("\n").split(s)) {
			if (line.startsWith("#")) {
				continue;
			}
			sb.append(line);
			sb.append("\n");
		}
		return sb.toString();
	}

	public static Map<String, String> toMap(Properties properties) {
		Map<String, String> map = Maps.newHashMap();
		for (Entry<Object, Object> entry : properties.entrySet()) {
			map.put((String) entry.getKey(), (String) entry.getValue());
		}
		return map;
	}

	public static Map<String, String> prefixProperties(Map<String, String> config, String prefix) {
		Map<String, String> prefixed = Maps.newHashMap();
		for (Entry<String, String> entry : config.entrySet()) {
			prefixed.put(prefix + entry.getKey(), entry.getValue());
		}
		return prefixed;
	}
}
