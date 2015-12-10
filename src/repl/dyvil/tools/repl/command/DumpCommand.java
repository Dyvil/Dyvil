package dyvil.tools.repl.command;

import dyvil.tools.repl.DyvilREPL;

import java.io.File;
import java.io.IOException;

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
	public void execute(DyvilREPL repl, String... args)
	{
		if (args.length == 0)
		{
			System.out.println("Result Class Dumping disabled.");
			repl.setDumpDir(null);
			return;
		}
		
		File dumpDir = new File(args[0]);
		repl.setDumpDir(dumpDir);
		
		try
		{
			String canonical = dumpDir.getCanonicalPath();
			System.out.println("Dumping Result Classes to '" + canonical + "'");
		}
		catch (IOException ex)
		{
			String absolute = dumpDir.getAbsolutePath();
			System.err.println("Invalid Dumping Path: " + absolute);
		}
	}
}
