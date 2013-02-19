package com.fathomdb.cli.output;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.fathomdb.cli.CliContext;
import com.fathomdb.cli.formatter.FormatterRegistry;
import com.google.common.collect.Lists;

public class FormattedList<T> implements Iterable<T> {
	private static final long serialVersionUID = 1L;
	private final FormatterRegistry registry;

	final boolean decorate;

	final List<T> items;
	final CliContext context;

	public FormattedList(CliContext context, FormatterRegistry registry, Iterable<T> items, boolean decorate) {
		this.context = context;
		this.items = Lists.newArrayList(items);
		this.registry = registry;
		this.decorate = decorate;
	}

	public FormattedList(CliContext context, FormatterRegistry registry, boolean decorate) {
		this(context, registry, Collections.<T> emptyList(), decorate);
	}

	public static <T> FormattedList<T> build(CliContext context, FormatterRegistry registry, Iterable<T> items,
			boolean decorate) {
		return new FormattedList<T>(context, registry, items, decorate);
	}

	@Override
	public String toString() {
		Writer writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		TextOutputSink sink = new TextOutputSink(context, registry, out, decorate);

		try {
			for (T item : this) {
				sink.visitObject(item);
			}
			sink.finishOutput();
		} catch (IOException e) {
			throw new IllegalArgumentException("Error formatting list", e);
		}

		return writer.toString();
	}

	@Override
	public Iterator<T> iterator() {
		return items.iterator();
	}
}
