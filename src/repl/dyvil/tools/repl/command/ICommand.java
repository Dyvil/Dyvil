package dyvil.tools.repl.command;

import dyvil.tools.repl.DyvilREPL;

public interface ICommand
{
	String getName();

	default String[] getAliases()
	{
		return null;
	}
	
	String getDescription();

	String getUsage();
	
	void execute(DyvilREPL repl, String args);
}
