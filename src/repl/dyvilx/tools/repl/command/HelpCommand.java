package dyvilx.tools.repl.command;

import dyvil.collection.Entry;
import dyvil.collection.Map;
import dyvil.collection.Set;
import dyvil.collection.mutable.HashMap;
import dyvil.collection.mutable.TreeMap;
import dyvil.collection.mutable.TreeSet;
import dyvilx.tools.repl.DyvilREPL;
import dyvilx.tools.repl.lang.I18n;

import java.io.PrintStream;

public class HelpCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "help";
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
		output.println(I18n.get("command.help.available"));

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
			output.printf("%1$" + maxLength + "s - %2$s\n", entry.getKey(), getDesc(entry.getValue().getName()));
		}
	}

	private void printCommandInfo(DyvilREPL repl, String commandName)
	{
		final ICommand command = DyvilREPL.getCommands().get(commandName);
		if (command == null)
		{
			repl.getErrorOutput().println(I18n.get("command.not_found", commandName));
			return;
		}

		final PrintStream output = repl.getOutput();

		output.println(I18n.get("command.help.header", command.getName(), getDesc(commandName)));
		output.println(I18n.get("command.help.usage", command.getUsage()));

		final String[] aliases = command.getAliases();
		if (aliases != null && aliases.length > 0)
		{
			output.println(I18n.get("command.help.aliases", String.join(", ", (CharSequence[]) aliases)));
		}
	}

	private static String getDesc(String commandName)
	{
		return I18n.get("command." + commandName + ".desc");
	}
}
