package com.fathomdb.meta;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.fathomdb.io.IoUtils;

public class Meta<T> {
	final Class<T> clazz;

	final List<MetaField<T>> allFields;
	final MetaField<T>[] identityFields;
	final List<MetaField<T>> toStringFields;
	final List<MetaField<T>> closeableFields;

	public Meta(Class<T> c) {
		this.clazz = c;

		this.allFields = findFields();
		this.identityFields = toArray(allFields);
		this.toStringFields = allFields;
		this.closeableFields = filter(allFields, Closeable.class);
	}

	private static <T> MetaField<T>[] toArray(List<MetaField<T>> list) {
		return list.toArray(new MetaField[list.size()]);
	}

	private List<MetaField<T>> filter(List<MetaField<T>> fields, Class<?> checkClass) {
		List<MetaField<T>> matches = new ArrayList<MetaField<T>>();
		for (MetaField<T> field : fields) {
			Class<?> fieldType = field.field.getType();
			if (checkClass.isAssignableFrom(fieldType)) {
				matches.add(field);
			}
		}

		return matches;
	}

	private List<MetaField<T>> findFields() {
		List<MetaField<T>> metaFields = new ArrayList<MetaField<T>>();

		Class<?> current = clazz;
		while (current != null) {
			for (Field field : current.getDeclaredFields()) {
				int modifiers = field.getModifiers();
				if ((modifiers & Modifier.STATIC) != 0) {
					continue;
				}

				metaFields.add(new MetaField<T>(field));
			}

			current = current.getSuperclass();
		}

		return metaFields;
	}

	public static <T> Meta<T> get(Class<T> c) {
		return new Meta<T>(c);
	}

	public int hashCode(T o) {
		final int prime = 31;

		int result = 1;

		for (int i = 0; i < identityFields.length; i++) {
			MetaField<T> field = identityFields[i];
			result = (prime * result) + field.hashCode(o);
		}

		return result;
	}

	public boolean equals(T a, Object bObject) {
		if (a == bObject) {
			return true;
		}
		if (a == null || bObject == null) {
			return false;
		}
		if (a.getClass() != bObject.getClass()) {
			return false;
		}
		T b = (T) bObject;

		for (int i = 0; i < identityFields.length; i++) {
			MetaField<T> field = identityFields[i];
			if (!field.equals(a, b)) {
				return false;
			}
		}

		return true;
	}

	public String toString(T o) {
		StringBuilder sb = new StringBuilder();

		sb.append(clazz.getSimpleName());
		sb.append('[');
		for (int i = 0; i < toStringFields.size(); i++) {
			MetaField<T> field = toStringFields.get(i);
			if (i != 0) {
				sb.append(',');
			}
			sb.append(field.getToStringName());
			sb.append('=');
			Object fieldValue = field.getValue(o);
			if (fieldValue == null) {
				sb.append("null");
			} else {
				sb.append(fieldValue.toString());
			}
		}
		sb.append(']');

		return sb.toString();
	}

	public void closeAll(T item) {
		for (MetaField<T> closeableField : closeableFields) {
			Closeable value = (Closeable) closeableField.getValue(item);
			if (value == null) {
				continue;
			}

			IoUtils.safeClose(value);
			closeableField.setValue(item, null);
		}
	}

}
