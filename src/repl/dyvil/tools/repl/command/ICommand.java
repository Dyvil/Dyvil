package dyvil.tools.repl.command;

import dyvil.tools.repl.DyvilREPL;

public interface ICommand
{
	String getName();
	
	String getDescription();
	
	void execute(DyvilREPL repl, String... args);
}
