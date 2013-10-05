package io.fathom.http;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import com.google.common.io.ByteSource;

public interface HttpRequest {
    void setHeader(String key, String value);

    List<String> getRequestHeaders(String key);

    HttpResponse doRequest() throws IOException;

    URI getUrl();

    void setRequestContent(ByteSource data) throws IOException;

    HttpMethod getMethod();
}
