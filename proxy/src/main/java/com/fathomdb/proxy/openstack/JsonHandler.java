package com.fathomdb.proxy.openstack;

public interface JsonHandler {

	void endArray();

	void beginObject();

	void gotValue(ValueType type, String value);

	enum ValueType {
		LiteralTrue, LiteralFalse, LiteralNull, String, Number
	}

	void beginArray();

	void endObject();

	void gotKey(String value);

	void endDocument();
}
