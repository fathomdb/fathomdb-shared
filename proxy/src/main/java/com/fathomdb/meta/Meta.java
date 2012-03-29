package com.fathomdb.meta;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import com.google.common.collect.Lists;

public class Meta<T> {
	final Class<T> clazz;

	final MetaField<T>[] identityFields;
	final MetaField<T>[] toStringFields;

	public Meta(Class<T> c) {
		this.clazz = c;
		this.identityFields = findIdentityFields();
		this.toStringFields = identityFields;
	}

	private MetaField<T>[] findIdentityFields() {
		List<MetaField<T>> metaFields = Lists.newArrayList();

		Class<?> current = clazz;
		while (current != null) {
			for (Field field : current.getDeclaredFields()) {
				int modifiers = field.getModifiers();
				if ((modifiers & Modifier.STATIC) != 0) {
					continue;
				}
				MetaField<T> metaField = new MetaField<T>(clazz, field);
				metaFields.add(metaField);
			}

			current = current.getSuperclass();
		}

		return (MetaField<T>[]) metaFields.toArray(new MetaField[metaFields
				.size()]);
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
		if (a == bObject)
			return true;
		if (a == null || bObject == null)
			return false;
		if (a.getClass() != bObject.getClass())
			return false;
		T b = (T) bObject;

		for (int i = 0; i < identityFields.length; i++) {
			MetaField<T> field = identityFields[i];
			if (!field.equals(a, b))
				return false;
		}

		return true;
	}

	public String toString(T o) {
		StringBuilder sb = new StringBuilder();

		sb.append(clazz.getSimpleName());
		sb.append('[');
		for (int i = 0; i < toStringFields.length; i++) {
			MetaField<T> field = toStringFields[i];
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
}
