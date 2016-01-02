package dyvil.tools.repl.command;

import dyvil.collection.Entry;
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
		repl.getOutput().println("Available Commands: ");
		
		for (Entry<String, ICommand> entry : DyvilREPL.getCommands())
		{
			repl.getOutput().print(" ");
			repl.getOutput().print(entry.getKey());
			repl.getOutput().print(" - ");
			repl.getOutput().println(entry.getValue().getDescription());
		}
	}
}
