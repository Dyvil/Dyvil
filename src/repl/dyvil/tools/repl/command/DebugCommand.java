package dyvil.tools.repl.command;

import dyvil.tools.compiler.DyvilCompiler;

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
		return "enables or disables debug mode";
	}
	
	@Override
	public void execute(String... args)
	{
		DyvilCompiler.debug = !DyvilCompiler.debug;
		System.out.println("Setting debug mode to " + (DyvilCompiler.debug ? "ON" : "OFF"));
	}
}
