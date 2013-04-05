package com.fathomdb.cli.output;

import java.io.IOException;

import com.fathomdb.cli.CliContext;
import com.fathomdb.cli.formatter.DefaultFormatter;
import com.fathomdb.cli.formatter.Formatter;
import com.fathomdb.cli.formatter.FormatterRegistry;

public abstract class OutputSinkBase implements OutputSink {
	final CliContext context;
	final FormatterRegistry formatterRegistry;

	public OutputSinkBase(CliContext context) {
		this.context = context;
		this.formatterRegistry = context.getFormatterRegistry();
	}

	@Override
	public void visitObject(Object o) throws IOException {
		Formatter formatter = formatterRegistry.getFormatter(o.getClass());
		if (formatter == null) {
			formatter = DefaultFormatter.INSTANCE;
		}
		formatter.visitObject(context, o, this);
	}

}
