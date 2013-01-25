package com.fathomdb.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import jline.ConsoleReader;

import org.kohsuke.args4j.CmdLineException;

import com.fathomdb.Utf8;
import com.fathomdb.cli.commands.CommandRunner;
import com.fathomdb.cli.output.OutputSink;
import com.fathomdb.cli.output.RawOutputSink;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

class CliSimpleRepl implements Repl {
	private static final String PROMPT = "> ";

	private final CliContext context;
	private final OutputSink outputSink;

	private final PrintWriter err;
	private final ConsoleReader reader;

	private final OutputFormat format;

	public CliSimpleRepl(OutputFormat format, OutputSink outputSink, PrintWriter err, CliContext context)
			throws IOException {
		this.format = format;
		this.outputSink = outputSink;
		this.err = err;
		this.context = context;

		reader = new ConsoleReader();
		reader.setBellEnabled(false);
		// reader.setDebug(new PrintWriter(new FileWriter("writer.debug", true)));

		// reader.addCompletor(new ArgumentCompletor(completors));
	}

	@Override
	public void runRepl() throws IOException {
		// TODO: History
		// reader.setHistory(history)

		String line;
		while ((line = reader.readLine(PROMPT)) != null) {
			if (!doReplLine(line)) {
				break;
			}

		}
	}

	private boolean doReplLine(String line) {
		line = line.trim();

		if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("\\q")) {
			return false;
		}

		try {
			if (!executeCommand(tokenize(line))) {
				return false;
			}
		} catch (Exception e) {
			err.println("Error running command");
			e.printStackTrace(err);
			return false;
		}

		outputSink.flush();
		return true;
	}

	//
	// @Override
	// public void runCommands(List<String> commands) {
	// // TODO: History
	// // reader.setHistory(history)
	//
	// for (String command : commands) {
	// doReplLine(command);
	// }
	// }

	@Override
	public boolean runCommand(List<String> arguments) {
		try {
			if (!executeCommand(arguments)) {
				return false;
			}
		} catch (Exception e) {
			err.println("Error running command");
			e.printStackTrace(err);
			return false;
		}

		outputSink.flush();

		return true;
	}

	@Override
	public void close() throws IOException {
		// this.client.close();

		if (outputSink != null) {
			outputSink.flush();
		}
		if (err != null) {
			err.flush();
		}

		// reader.getHistory().setOutput();
	}

	protected boolean executeCommand(List<String> tokens) throws IOException {
		if (tokens.size() == 0) {
			return true;
		}

		String verb = tokens.get(0).trim();

		if (verb.length() == 0) {
			return true;
		}

		List<String> args = null;

		if (tokens.size() != 1) {
			args = tokens.subList(1, tokens.size());
		} else {
			args = Collections.emptyList();
		}

		CommandRunner commandRunner = context.getCommandRegistry().getCommandRunner(verb);
		if (commandRunner == null) {
			err.println("Unknown command: " + verb + " (line=" + Joiner.on(" ").join(tokens) + ")");
			return false;
		}

		commandRunner.setOutputFormat(format);

		Object results;
		try {
			commandRunner = commandRunner.clone(context);
			commandRunner.parseArguments(args);

			results = commandRunner.runCommand();
		} catch (CmdLineException e) {
			err.println(e.getMessage());
			e.getParser().printUsage(err, null);
			return false;
		} catch (CliException e) {
			err.println(e.getMessage());
			return false;
		} catch (Exception e) {
			err.println("Error running command: " + Joiner.on(" ").join(tokens));
			e.printStackTrace(err);
			return false;
		}

		if (results != null) {
			outputResults(commandRunner, results);
		}
		return true;
	}

	protected void outputResults(CommandRunner commandRunner, Object results) throws IOException {
		if (outputSink instanceof RawOutputSink) {
			commandRunner.formatRaw(results, ((RawOutputSink) outputSink).getWriter());
		} else {
			Object formatted = commandRunner.convertToOutputFormat(results);
			if (formatted instanceof Iterable) {
				for (Object item : (Iterable) formatted) {
					outputSink.visitObject(item);
				}
			} else {
				outputSink.visitObject(formatted);
			}
		}

		outputSink.finishOutput();
		outputSink.flush();
	}

	public boolean runScripts(List<File> scriptFiles) throws IOException {
		for (File scriptFile : scriptFiles) {
			BufferedReader reader = new BufferedReader(Utf8.openFile(scriptFile));
			try {
				while (true) {
					String line = reader.readLine();
					if (line == null) {
						continue;
					}
					if (line.startsWith("--")) {
						continue;
					}

					if (line.length() == 0) {
						continue;
					}

					if (!executeCommand(tokenize(line))) {
						return false;
					}
				}
			} finally {
				Closeables.closeQuietly(reader);
			}
		}
		return true;
	}

	private List<String> tokenize(String line) {
		// TODO: Quoting
		List<String> tokens = Lists.newArrayList(line.split(" "));
		return tokens;
	}
}
