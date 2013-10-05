package io.fathom.rest;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;

public class HttpEntity {
    final String contentType;
    final ByteSource content;

    public HttpEntity(String contentType, ByteSource content) {
        this.contentType = contentType;
        this.content = content;
    }

    public static HttpEntity asXml(Object object) throws RestClientException {
        try {
            boolean formatted = false;
            String content = JaxbXmlCodec.toXml(object, formatted);

            return fromStringUtf8("application/xml", content);
        } catch (Exception e) {
            throw new RestClientException("Error serializing data", e);
        }
    }

    private static HttpEntity fromStringUtf8(String contentType, String content) {
        return new HttpEntity(contentType, ByteSource.wrap(content.getBytes(Charsets.UTF_8)));
    }

    public String getContentType() {
        return contentType;
    }

    public ByteSource getContent() {
        return content;
    }

}
