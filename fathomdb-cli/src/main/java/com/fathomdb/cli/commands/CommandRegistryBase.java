package com.fathomdb.cli.commands;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import com.google.common.collect.Lists;

public class CommandRegistryBase implements CommandRegistry {
	final Map<String, CommandRunner> registry = new HashMap<String, CommandRunner>();

	@Override
	public CommandRunner getCommandRunner(String command) {
		command = command.trim();
		return registry.get(command.toLowerCase());
	}

	protected void addCommand(CommandRunner commandRunner) {
		for (CommandSpecifier command : commandRunner.getHandledComands()) {
			for (String commandString : command.getStrings()) {
				registry.put(commandString.toLowerCase(), commandRunner);
			}
		}
	}

	@Override
	public List<String> listCommands() {
		List<String> commands = Lists.newArrayList();
		for (CommandRunner key : registry.values()) {
			for (CommandSpecifier commandSpecifier : key.getHandledComands()) {
				List<String> commandStrings = commandSpecifier.getStrings();
				// We only add the 'primary' command for now
				String commandString = commandStrings.get(0);
				if (!commands.contains(commandString)) {
					commands.add(commandString);
				}
			}
		}

		Collections.sort(commands);

		return commands;
	}

	protected void discoverCommands() {
		ServiceLoader<CommandRunner> serviceLoader = ServiceLoader.load(CommandRunner.class);
		for (CommandRunner commandRunner : serviceLoader) {
			addCommand(commandRunner);
		}
	}

	// protected void discoverCommands() {
	// Discovery discovery = Discovery.build();
	//
	// Collection<Class> classes = discovery.findAnnotatedClasses(Cmdlet.class);
	// discoverCommands(classes);
	// }
	//
	// protected void discoverCommands(Iterable<Class> classes) {
	// if (classes == null) {
	// return;
	// }
	//
	// List<CommandRunner> instances = Discovery.buildInstances(CommandRunner.class, classes);
	// for (CommandRunner commandRunner : instances) {
	// addCommand(commandRunner);
	// }
	// }
}
