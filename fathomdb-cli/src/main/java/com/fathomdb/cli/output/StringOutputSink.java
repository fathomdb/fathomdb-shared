package com.fathomdb.cli.output;

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.Iterables;

public class StringOutputSink implements OutputSink {

	final StringBuilder builder = new StringBuilder();

	@Override
	public void visitObject(Object o) throws IOException {
		if (o == null) {
			builder.append("-");
		} else {
			builder.append(o.toString());
		}
	}

	@Override
	public void outputRow(Map<String, Object> values) throws IOException {
		if (values.size() == 0) {
			return;
		}
		if (values.size() == 1) {
			Object o = Iterables.get(values.values(), 0);
			visitObject(o);
			return;
		}
		throw new UnsupportedOperationException();

	}

	@Override
	public void flush() {

	}

	@Override
	public void finishOutput() {

	}

	public String getString() {
		return builder.toString();
	}

}
