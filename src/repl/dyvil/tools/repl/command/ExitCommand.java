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
	public void execute(DyvilREPL repl, String argument)
	{
		if (argument == null)
		{
			System.exit(0);
			return;
		}

		try
		{
			System.exit(Integer.parseInt(argument));
		}
		catch (NumberFormatException ex)
		{
			repl.getErrorOutput().println("Invalid Exit Code " + argument);
			return;
		}
		return;
	}
}
