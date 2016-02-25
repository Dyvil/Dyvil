package dyvil.tools.repl.command;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.repl.DyvilREPL;

public class VersionCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "version";
	}
	
	@Override
	public String getDescription()
	{
		return "Prints information about the current Dyvil Installation";
	}

	@Override
	public String getUsage()
	{
		return ":version";
	}

	@Override
	public void execute(DyvilREPL repl, String... args)
	{
		repl.getOutput().print("Dyvil version:\t\t");
		repl.getOutput().println(DyvilCompiler.DYVIL_VERSION);
		repl.getOutput().print(" Library version:\t");
		repl.getOutput().println(DyvilCompiler.LIBRARY_VERSION);
		repl.getOutput().print(" Compiler version:\t");
		repl.getOutput().println(DyvilCompiler.VERSION);
		repl.getOutput().print(" REPL version:\t\t");
		repl.getOutput().println(DyvilREPL.VERSION);
	}
}
