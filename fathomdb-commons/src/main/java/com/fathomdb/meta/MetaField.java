package com.fathomdb.meta;

import java.lang.reflect.Field;

public class MetaField<T> {
	final Field field;

	public MetaField(Field field) {
		super();
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
			throw new IllegalStateException("Error getting field: " + field, e);
		}
		return fieldValue;
	}

	public void setValue(T o, Object value) {
		try {
			field.set(o, value);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Error setting field: " + field, e);
		}
	}

	@Override
	public String toString() {
		return "MetaField [field=" + field + "]";
	}

	public String getToStringName() {
		return field.getName();
	}

}
