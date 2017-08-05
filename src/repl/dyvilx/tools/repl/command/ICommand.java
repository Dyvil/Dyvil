package dyvilx.tools.repl.command;

import dyvilx.tools.repl.DyvilREPL;

public interface ICommand
{
	String getName();

	default String[] getAliases()
	{
		return null;
	}

	String getUsage();
	
	void execute(DyvilREPL repl, String args);
}
