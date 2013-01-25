package com.fathomdb.cli;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public interface Repl extends Closeable {
	boolean runCommand(List<String> arguments);

	void runRepl() throws IOException;
}
