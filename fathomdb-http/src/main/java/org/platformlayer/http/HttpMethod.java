package org.platformlayer.http;

import com.fathomdb.utils.EnumUtils;

public enum HttpMethod {
	GET, POST, PUT, DELETE, OPTIONS;

	public String getHttpMethod() {
		return name();
	}

	public static HttpMethod tryParse(String method) {
		return EnumUtils.valueOfCaseInsensitive(HttpMethod.class, method);
	}
}
