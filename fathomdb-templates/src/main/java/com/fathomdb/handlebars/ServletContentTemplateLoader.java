package com.fathomdb.handlebars;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jknack.handlebars.TemplateLoader;
import com.google.common.base.Charsets;

class ServletContentTemplateLoader extends TemplateLoader {

	private static final Logger log = LoggerFactory.getLogger(ServletContentTemplateLoader.class);

	final String basePath;

	public ServletContentTemplateLoader(String basePath) {
		this.basePath = basePath;
	}

	public String resolve(String path) {
		log.info("Resolving template: " + path);

		if (!path.endsWith(".hbs")) {
			path += ".hbs";
		}

		if (!path.startsWith("/")) {
			path = "/" + path;
		}

		if (basePath != null) {
			if (!path.startsWith(basePath)) {
				path = basePath + path;
			}
		}

		{
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			String loadPath = path;
			if (loadPath.startsWith("/")) {
				loadPath = path.substring(1);
			}
			URL resource = contextClassLoader.getResource(loadPath);
			if (resource != null) {
				return loadPath;
			}
		}

		return null;
	}

	@Override
	protected Reader read(String path) throws IOException {
		if (path == null) {
			return null;
		}

		log.info("Loading template: " + path);

		InputStream is = null;

		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		is = contextClassLoader.getResourceAsStream(path);

		if (is != null) {
			return new InputStreamReader(is, Charsets.UTF_8);
		}

		return null;
	}

}