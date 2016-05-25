package dyvil.tools.repl.command;

import dyvil.tools.repl.DyvilREPL;
import dyvil.tools.repl.lang.I18n;

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
	public String getUsage()
	{
		return ":dump [path]";
	}

	@Override
	public void execute(DyvilREPL repl, String argument)
	{
		if (argument == null)
		{
			repl.getOutput().println(I18n.get("command.dump.disabled"));
			repl.setDumpDir(null);
			return;
		}
		
		File dumpDir = new File(argument);
		repl.setDumpDir(dumpDir);
		
		try
		{
			repl.getOutput().println(I18n.get("command.dump.enabled", dumpDir.getCanonicalPath()));
		}
		catch (IOException ex)
		{
			String absolute = dumpDir.getAbsolutePath();
			repl.getErrorOutput().println(I18n.get("command.dump.invalid", absolute));
		}
	}
}
