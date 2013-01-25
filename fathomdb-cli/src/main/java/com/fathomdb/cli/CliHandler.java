package com.fathomdb.cli;

import com.fathomdb.cli.commands.CommandRegistry;

public interface CliHandler {

	CliOptions buildOptionsBean();

	CliContext buildContext(CommandRegistry commandRegistry, CliOptions options) throws Exception;

	CommandRegistry buildCommandRegistry();
}
