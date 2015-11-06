package dyvil.tools.repl.command;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.repl.DyvilREPL;

public class DebugCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "debug";
	}
	
	@Override
	public String getDescription()
	{
		return "Enables or disables Debug Mode";
	}
	
	@Override
	public void execute(DyvilREPL repl, String... args)
	{
		DyvilCompiler.debug = !DyvilCompiler.debug;
		System.out.println("Setting debug mode to " + (DyvilCompiler.debug ? "ON" : "OFF"));
	}
}
