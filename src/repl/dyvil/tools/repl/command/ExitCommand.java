package dyvil.tools.repl.command;

import dyvil.tools.repl.DyvilREPL;

public class ExitCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "exit";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "q", "quit" };
	}

	@Override
	public String getDescription()
	{
		return "Exits the current REPL instance";
	}

	@Override
	public String getUsage()
	{
		return ":<exit|q|quit> [exitcode]";
	}

	@Override
	public void execute(DyvilREPL repl, String... args)
	{
		if (args.length == 0)
		{
			System.exit(0);
			return;
		}
		
		int code = 0;
		try
		{
			code = Integer.parseInt(args[0]);
		}
		catch (NumberFormatException ex)
		{
			repl.getOutput().println("Invalid Exit Code " + args[0]);
		}
		System.exit(code);
		return;
	}
}
