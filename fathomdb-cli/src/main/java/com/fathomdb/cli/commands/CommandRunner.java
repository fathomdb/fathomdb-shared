package com.fathomdb.cli.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.fathomdb.cli.CliContext;
import com.fathomdb.cli.OutputFormat;
import com.fathomdb.cli.autocomplete.AutoCompletor;
import com.fathomdb.cli.output.OutputSink;

public interface CommandRunner {
	CommandRunner clone(CliContext context);

	Object runCommand() throws Exception;

	List<CommandSpecifier> getHandledComands();

	void parseArguments(List<String> args) throws Exception;

	void formatRaw(Object o, PrintWriter writer);

	AutoCompletor getAutoCompleter();

	Object convertToOutputFormat(Object results);

	void setOutputFormat(OutputFormat format);

	void outputResults(OutputSink sink, Object results) throws IOException;
}
