package com.fathomdb.meta;

import java.lang.reflect.Field;

public class MetaField<T> {
	final Class<T> clazz;
	final Field field;

	public MetaField(Class<T> clazz, Field field) {
		super();
		this.clazz = clazz;
		this.field = field;

		setAccessible();
	}

	private void setAccessible() {
		field.setAccessible(true);
	}

	public int hashCode(T o) {
		Object fieldValue = getValue(o);
		if (fieldValue == null) {
			return 0;
		} else {
			return fieldValue.hashCode();
		}
	}

	public boolean equals(T a, T b) {
		Object aValue = getValue(a);
		Object bValue = getValue(b);

		if (aValue == bValue) {
			return true;
		}

		if (aValue == null || bValue == null) {
			return false;
		}

		return aValue.equals(bValue);
	}

	Object getValue(T o) {
		Object fieldValue;
		try {
			fieldValue = field.get(o);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Error fetching field: " + field, e);
		}
		return fieldValue;
	}

	@Override
	public String toString() {
		return "MetaField [field=" + field + "]";
	}

	public String getToStringName() {
		return field.getName();
	}

}
