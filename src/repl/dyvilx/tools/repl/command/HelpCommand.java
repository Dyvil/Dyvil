package dyvilx.tools.repl.command;

import dyvilx.tools.repl.DyvilREPL;
import dyvilx.tools.repl.lang.I18n;

import java.io.PrintStream;
import java.util.Set;
import java.util.TreeSet;

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

		final Set<String> commands = new TreeSet<>();

		for (ICommand command : DyvilREPL.getCommands().values())
		{
			commands.add(command.getName());
		}

		int maxLength = 0;

		for (String name : commands)
		{
			final int length = name.length();
			if (length > maxLength)
			{
				maxLength = length;
			}
		}

		final String format = ":%1$-" + maxLength + "s - %2$s\n";
		for (String name : commands)
		{
			output.printf(format, name, getDesc(name));
		}
	}

	private void printCommandInfo(DyvilREPL repl, String commandName)
	{
		final ICommand command = DyvilREPL.getCommands().get(commandName);
		if (command == null)
		{
			repl.getCompiler().warn(I18n.get("command.not_found", commandName));
			return;
		}

		final PrintStream output = repl.getOutput();

		output.println(I18n.get("command.help.header", command.getName(), getDesc(commandName)));
		output.println(I18n.get("command.help.usage", command.getUsage()));

		final String[] aliases = command.getAliases();
		if (aliases != null && aliases.length > 0)
		{
			output.println(I18n.get("command.help.aliases", String.join(", ", aliases)));
		}
	}

	private static String getDesc(String commandName)
	{
		return I18n.get("command." + commandName + ".desc");
	}
}
