package com.fathomdb.discovery;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

public abstract class Discovery {

	public static <T> List<T> buildInstances(Class<T> baseClass, Iterable<Class> classes) {
		List<T> instances = Lists.newArrayList();

		for (Class<?> clazz : classes) {
			int modifiers = clazz.getModifiers();
			if ((modifiers & Modifier.ABSTRACT) != 0) {
				continue;
			}
			if (!baseClass.isAssignableFrom(clazz)) {
				continue;
			}
			T instance;
			try {
				instance = (T) clazz.newInstance();
			} catch (InstantiationException e) {
				throw new IllegalStateException("Error instantiating class: " + clazz, e);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Error instantiating class: " + clazz, e);
			}
			instances.add(instance);
		}
		return instances;
	}

	public static Discovery build() {
		return new ManifestDiscovery();
	}

	public abstract <T> Collection<Class> getSubTypesOf(Class<T> class1);

	public abstract List<Class> findClassesInPackage(Package package1);

	public abstract <T extends Annotation> Collection<Class> findAnnotatedClasses(Class<T> class1);

}
