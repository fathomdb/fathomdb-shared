package com.fathomdb.cli;

import java.io.PrintWriter;

import com.fathomdb.cli.output.ActionOutputSink;
import com.fathomdb.cli.output.OutputSink;
import com.fathomdb.cli.output.RawOutputSink;
import com.fathomdb.cli.output.TextOutputSink;

public enum OutputFormat {
	Text, Xml, Json, Raw, Action;

	public OutputSink buildOutputSink(CliContext context, PrintWriter out) {
		switch (this) {
		case Json:
			return new RawOutputSink(context, out);
		case Xml:
			return new RawOutputSink(context, out);
			// return new XmlOutputSink(out);
		case Text:
			return new TextOutputSink(context, out, true);
		case Raw:
			return new RawOutputSink(context, out);
		case Action:
			return new ActionOutputSink(context, out);
		default:
			throw new IllegalStateException();
		}
	}
}