package dyvil.tools.repl.command;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.repl.DyvilREPL;
import dyvil.tools.repl.lang.I18n;

public class VersionCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "version";
	}

	@Override
	public String getUsage()
	{
		return ":version";
	}

	@Override
	public void execute(DyvilREPL repl, String args)
	{
		repl.getOutput().println(I18n.get("command.version.dyvil", DyvilCompiler.DYVIL_VERSION));
		repl.getOutput().println(I18n.get("command.version.library", DyvilCompiler.LIBRARY_VERSION));
		repl.getOutput().println(I18n.get("command.version.compiler", DyvilCompiler.VERSION));
		repl.getOutput().println(I18n.get("command.version.repl", DyvilREPL.VERSION));
	}
}
