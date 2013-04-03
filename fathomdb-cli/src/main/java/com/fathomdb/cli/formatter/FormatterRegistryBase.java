package com.fathomdb.cli.formatter;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class FormatterRegistryBase implements FormatterRegistry {
	final Map<Class<?>, Formatter> registry = new HashMap<Class<?>, Formatter>();

	@Override
	public Formatter getFormatter(Class<?> clazz) {
		Formatter formatter = registry.get(clazz);
		if (formatter != null) {
			return formatter;
		}

		for (Class<?> interfaceClass : clazz.getInterfaces()) {
			formatter = registry.get(interfaceClass);
			if (formatter != null) {
				return formatter;
			}
		}

		return null;
	}

	protected void addDefaultFormatters() {
		addFormatter(new StringFormatter());
		addFormatter(new ClientActionDefaultFormatter());
	}

	protected void addFormatter(Formatter formatter) {
		for (Class<?> command : formatter.getHandledClasses()) {
			registry.put(command, formatter);
		}
	}

	protected void discoverFormatters() {
		ServiceLoader<Formatter> serviceLoader = ServiceLoader.load(Formatter.class);
		for (Formatter formatter : serviceLoader) {
			addFormatter(formatter);
		}
	}

	// protected void discoverFormatters() {
	// Discovery discovery = Discovery.build();
	// Collection<Class> classes = discovery.findAnnotatedClasses(Formatlet.class);
	// discoverFormatters(classes);
	// }
	//
	// protected void discoverFormatters(Iterable<Class> classes) {
	// List<Formatter> formatters = Discovery.buildInstances(Formatter.class, classes);
	// for (Formatter formatter : formatters) {
	// addFormatter(formatter);
	// }
	// }
}
