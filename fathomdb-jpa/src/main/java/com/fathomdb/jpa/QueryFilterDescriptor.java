package com.fathomdb.jpa;

public class QueryFilterDescriptor {

	final int paramIndex;
	final QueryFilter annotation;

	public QueryFilterDescriptor(int paramIndex, QueryFilter annotation) {
		this.paramIndex = paramIndex;
		this.annotation = annotation;
	}

	public String getSql() {
		assert !isLimit();
		assert !isOffset();
		return annotation.value();
	}

	public boolean isLimit() {
		return QueryFilter.LIMIT.equals(annotation.value());
	}

	public boolean isOffset() {
		return QueryFilter.OFFSET.equals(annotation.value());
	}

}
