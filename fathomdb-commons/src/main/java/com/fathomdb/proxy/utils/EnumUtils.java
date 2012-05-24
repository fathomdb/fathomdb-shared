package com.fathomdb.proxy.utils;

import java.util.EnumSet;

public class EnumUtils {

	public static <T extends Enum<T>> T valueOfCaseInsensitive(Class<T> enumType, String name) {
		if (name == null) {
			return null;
		}
		T t = valueOfCaseInsensitiveOrNull(enumType, name);
		if (t == null) {
			throw new IllegalArgumentException("Unknown value for " + enumType + ": " + name);
		}
		return t;
	}

	public static <T extends Enum<T>> T valueOfOrNull(Class<T> enumType, String name) {
		if (name == null) {
			return null;
		}
		for (T enumValue : EnumSet.allOf(enumType)) {
			if (enumValue.toString().equals(name)) {
				return enumValue;
			}
		}
		return null;
	}

	public static <T extends Enum<T>> T valueOfCaseInsensitiveOrNull(Class<T> enumType, String name) {
		if (name == null) {
			return null;
		}
		for (T enumValue : EnumSet.allOf(enumType)) {
			if (enumValue.toString().equalsIgnoreCase(name)) {
				return enumValue;
			}
		}
		return null;
	}

}
