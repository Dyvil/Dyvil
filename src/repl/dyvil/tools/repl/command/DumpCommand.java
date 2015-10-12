package dyvil.tools.repl.command;

import java.io.File;
import java.io.IOException;

import dyvil.tools.repl.DyvilREPL;

public class DumpCommand implements ICommand
{
	
	@Override
	public String getName()
	{
		return "dump";
	}
	
	@Override
	public String getDescription()
	{
		return "Sets the directory for Result Class Dumping.";
	}
	
	@Override
	public void execute(String... args)
	{
		if (args.length == 0)
		{
			System.out.println("Result Class Dumping disabled.");
			DyvilREPL.dumpDir = null;
			return;
		}
		
		DyvilREPL.dumpDir = new File(args[0]);
		
		try
		{
			String canonical = DyvilREPL.dumpDir.getCanonicalPath();
			System.out.println("Dumping Result Classes to '" + canonical + "'");
		}
		catch (IOException ex)
		{
			String absolute = DyvilREPL.dumpDir.getAbsolutePath();
			System.err.println("Invalid Dumping Path: " + absolute);
		}
	}
	
}
