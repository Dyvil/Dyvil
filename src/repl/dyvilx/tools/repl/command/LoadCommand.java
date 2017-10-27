package dyvilx.tools.repl.command;

import dyvilx.tools.repl.DyvilREPL;
import dyvilx.tools.repl.lang.I18n;

import java.io.File;

public class LoadCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "load";
	}

	@Override
	public String getUsage()
	{
		return ":load <file>";
	}

	@Override
	public void execute(DyvilREPL repl, String args)
	{
		File file = new File(args);
		if (!file.exists())
		{
			repl.getCompiler().warn(I18n.get("command.load.invalid", args));
			return;
		}

		repl.processFile(file);
	}
}
