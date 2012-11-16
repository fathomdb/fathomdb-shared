package org.platformlayer.http;

import java.io.IOException;
import java.net.URI;

import com.fathomdb.io.ByteSource;

public interface HttpRequest {
	void setRequestHeader(String key, String value);

	HttpResponse doRequest() throws IOException;

	URI getUrl();

	void setRequestContent(ByteSource data) throws IOException;
}
