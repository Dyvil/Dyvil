package dyvil.tools.repl.command;

import dyvil.collection.Entry;
import dyvil.collection.Map;
import dyvil.collection.Set;
import dyvil.collection.mutable.HashMap;
import dyvil.collection.mutable.TreeMap;
import dyvil.collection.mutable.TreeSet;
import dyvil.tools.repl.DyvilREPL;

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
	public void execute(DyvilREPL repl, String... args)
	{
		repl.getOutput().println("Available Commands:");

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
			repl.getOutput().printf("%1$" + maxLength + "s - %2$s\n", entry.getKey(), entry.getValue().getDescription());
		}
	}
}
