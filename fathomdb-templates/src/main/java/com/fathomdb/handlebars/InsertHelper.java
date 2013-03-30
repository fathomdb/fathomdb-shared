package com.fathomdb.handlebars;

import java.io.IOException;

import org.apache.commons.lang3.Validate;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;

public class InsertHelper implements Helper<Object> {

	public static final Helper<Object> INSTANCE = new InsertHelper();

	public static final String NAME = "insert";

	@Override
	public CharSequence apply(final Object context, final Options options) throws IOException {
		Validate.isTrue(context instanceof Template, "found '%s', expected 'template'", context);

		Template template = (Template) context;
		String v = template.apply(options.context);
		return v;

	}
}
