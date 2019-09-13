package dyvilx.tools.repl.command;

import dyvilx.tools.repl.DyvilREPL;
import dyvilx.tools.repl.lang.I18n;

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
			repl.getErrorOutput().println(I18n.get("command.exit.invalid", argument));
		}
	}
}
