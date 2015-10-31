package dyvil.tools.repl.command;

import dyvil.tools.repl.DyvilREPL;

public interface ICommand
{
	public String getName();
	
	public String getDescription();
	
	public void execute(DyvilREPL repl, String... args);
}
