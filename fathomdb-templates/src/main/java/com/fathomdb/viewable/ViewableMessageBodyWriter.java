package com.fathomdb.viewable;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import com.fathomdb.handlebars.HandlebarsTemplating;

public class ViewableMessageBodyWriter implements MessageBodyWriter<Viewable> {
    final HandlebarsTemplating handlebars;

    public ViewableMessageBodyWriter() {
        this.handlebars = HandlebarsTemplating.get();
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (Viewable.class.isAssignableFrom(type)) {
            return true;
        }

        return false;
    }

    @Override
    public long getSize(Viewable t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Viewable viewable, Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream out) throws IOException,
            WebApplicationException {
        Object model = viewable.getModel();

        String resolvedPath = handlebars.resolve(viewable.getPath());
        handlebars.writeTo(resolvedPath, model, out);
    }

}
