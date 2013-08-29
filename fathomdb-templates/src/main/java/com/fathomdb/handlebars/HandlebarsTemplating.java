package com.fathomdb.handlebars;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.TemplateCache;
import com.github.jknack.handlebars.cache.ConcurrentMapCache;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

public class HandlebarsTemplating {
    private static final Logger log = LoggerFactory.getLogger(HandlebarsTemplating.class);

    final String basePath = "/views";

    final Handlebars handlebars;

    final ServletContentTemplateLoader loader;

    // public static HandlebarsTemplating get(ServletContext servletContext) {
    // String attributeKey = HandlebarsTemplating.class.getName();
    // HandlebarsTemplating templating = (HandlebarsTemplating)
    // servletContext.getAttribute(attributeKey);
    // if (templating == null) {
    // synchronized (servletContext) {
    // templating = (HandlebarsTemplating)
    // servletContext.getAttribute(attributeKey);
    //
    // if (templating == null) {
    // templating = new HandlebarsTemplating(servletContext);
    // servletContext.setAttribute(attributeKey, templating);
    // }
    // }
    // }
    // return templating;
    // }

    static HandlebarsTemplating INSTANCE = new HandlebarsTemplating();

    public static HandlebarsTemplating get() {
        return INSTANCE;
    }

    public HandlebarsTemplating() {
        // this.servletContext = servletContext;

        TemplateCache cache = new ConcurrentMapCache();
        this.loader = new ServletContentTemplateLoader(basePath);
        loader.setSuffix("");
        loader.setPrefix("");
        this.handlebars = new Handlebars(loader, cache);

        handlebars.registerHelper(InsertHelper.NAME, InsertHelper.INSTANCE);
    }

    public String resolve(String path) {
        try {
            return loader.resolve(path);
        } catch (Exception e) {
            log.warn("Error resolving template", e);
        }

        return null;
    }

    String tryReadResource(String path) {
        Reader reader = null;
        try {
            reader = loader.read(path);
            if (reader == null) {
                return null;
            }
            return CharStreams.toString(reader);
        } catch (IOException e) {
            log.warn("Error reading resource: " + path, e);
        } finally {
            Closeables.closeQuietly(reader);
        }
        return null;
    }

    public void writeTo(String resolvedPath, Object model, OutputStream out) throws IOException {
        String layoutPath = ("/layouts/application.hbs");
        Template layoutTemplate = handlebars.compile(URI.create(layoutPath));

        Template template = handlebars.compile(URI.create(resolvedPath));

        String pageCss = null;
        if (resolvedPath.endsWith(".hbs")) {
            String cssPath = resolvedPath.substring(0, resolvedPath.length() - 4) + ".css";
            pageCss = tryReadResource(cssPath);

            if (!Strings.isNullOrEmpty(pageCss)) {
                pageCss = "<style type=\"text/css\">" + pageCss + "</style>";
            }
        }

        com.github.jknack.handlebars.Context.Builder contextBuilder = com.github.jknack.handlebars.Context
                .newBuilder(model);
        contextBuilder = contextBuilder.resolver(MapValueResolver.INSTANCE, FieldValueResolver.INSTANCE);
        contextBuilder.combine("content", template);
        contextBuilder.combine("pageCss", pageCss);

        try {
            Writer writer = new OutputStreamWriter(out);
            layoutTemplate.apply(contextBuilder.build(), writer);
            writer.flush();
        } catch (Exception e) {
            throw new IOException("Error running template", e);
        }
    }
}
