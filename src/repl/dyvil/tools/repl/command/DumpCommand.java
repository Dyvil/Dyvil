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
		return "Sets the directory for Result Class Dumping";
	}

	@Override
	public String getUsage()
	{
		return ":dump [path]";
	}

	@Override
	public void execute(DyvilREPL repl, String... args)
	{
		if (args.length == 0)
		{
			repl.getOutput().println("Result Class Dumping disabled.");
			repl.setDumpDir(null);
			return;
		}
		
		File dumpDir = new File(args[0]);
		repl.setDumpDir(dumpDir);
		
		try
		{
			String canonical = dumpDir.getCanonicalPath();
			repl.getOutput().println("Dumping Result Classes to '" + canonical + "'");
		}
		catch (IOException ex)
		{
			String absolute = dumpDir.getAbsolutePath();
			repl.getErrorOutput().println("Invalid Dumping Path: " + absolute);
		}
	}
}
