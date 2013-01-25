package com.fathomdb.cli;

import org.kohsuke.args4j.Option;

public class CliOptions {

	@Option(name = "-f", aliases = "--format", usage = "output format")
	public OutputFormat format = OutputFormat.Text;

	@Option(name = "--shell", usage = "shell type")
	public ShellType shellType = ShellType.Simple;

	@Option(name = "-h", aliases = "--help", usage = "displays this help command")
	public boolean showHelp = false;

	boolean serverMode;

	public boolean isServerMode() {
		return serverMode;
	}

	public void setServerMode(boolean serverMode) {
		this.serverMode = serverMode;
	}

}
