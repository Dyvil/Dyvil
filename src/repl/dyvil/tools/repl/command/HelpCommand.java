package dyvil.tools.repl.command;

import dyvil.collection.Entry;
import dyvil.collection.Map;
import dyvil.collection.Set;
import dyvil.collection.mutable.HashMap;
import dyvil.collection.mutable.TreeMap;
import dyvil.collection.mutable.TreeSet;
import dyvil.tools.repl.DyvilREPL;

import java.io.PrintStream;

public class HelpCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "help";
	}
	
	@Override
	public String getDescription()
	{
		return "Shows this help text";
	}

	@Override
	public String getUsage()
	{
		return ":help [command]";
	}

	@Override
	public void execute(DyvilREPL repl, String argument)
	{
		if (argument == null)
		{
			this.printAllCommands(repl);
			return;
		}

		this.printCommandInfo(repl, argument);
	}

	private void printAllCommands(DyvilREPL repl)
	{
		final PrintStream output = repl.getOutput();
		output.println("Available Commands:");

		Map<ICommand, Set<String>> commands = new HashMap<>();

		for (Entry<String, ICommand> entry : DyvilREPL.getCommands())
		{
			final ICommand command = entry.getValue();
			Set<String> names = commands.get(command);
			if (names == null)
			{
				commands.put(command, names = new TreeSet<>());
			}
			names.add(entry.getKey());
		}

		Map<String, ICommand> result = new TreeMap<>();
		for (Entry<ICommand, Set<String>> entry : commands)
		{
			result.put(entry.getValue().toString("", ", ", ""), entry.getKey());
		}

		int maxLength = 0;
		for (String s : result.keys())
		{
			if (s.length() > maxLength)
			{
				maxLength = s.length();
			}
		}

		for (Entry<String, ICommand> entry : result)
		{
			output.printf("%1$" + maxLength + "s - %2$s\n", entry.getKey(), entry.getValue().getDescription());
		}
	}

	private void printCommandInfo(DyvilREPL repl, String commandName)
	{
		final ICommand command = DyvilREPL.getCommands().get(commandName);
		if (command == null)
		{
			repl.getErrorOutput().println("Command not found: " + commandName);
			return;
		}

		final PrintStream output = repl.getOutput();

		output.print(':');
		output.print(command.getName());
		output.print(" - ");
		output.println(command.getDescription());
		output.print("  Usage:\t");
		output.println(command.getUsage());

		final String[] aliases = command.getAliases();
		if (aliases != null && aliases.length > 0)
		{
			output.print("  Aliases:\t");
			output.print(aliases[0]);
			for (int i = 1; i < aliases.length; i++)
			{
				output.print(", ");
				output.print(aliases[i]);
			}

			output.println();
		}
	}
}
