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
	public void execute(String... args)
	{
		System.out.println("Available Commands: ");
		
		for (Entry<String, ICommand> entry : DyvilREPL.commands)
		{
			System.out.print(" ");
			System.out.print(entry.getKey());
			System.out.print(" - ");
			System.out.println(entry.getValue().getDescription());
		}
	}
	
}
