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
		return "Prints information about the current Dyvil Installation.";
	}
	
	@Override
	public void execute(String... args)
	{
		System.out.print("Dyvil version:\t\t");
		System.out.println(DyvilCompiler.DYVIL_VERSION);
		System.out.print(" Library version:\t");
		System.out.println(DyvilCompiler.LIBRARY_VERSION);
		System.out.print(" Compiler version:\t");
		System.out.println(DyvilCompiler.VERSION);
		System.out.print(" REPL version:\t\t");
		System.out.println(DyvilREPL.VERSION);
	}
}
