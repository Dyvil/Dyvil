package dyvilx.tools.repl.command;

import dyvilx.tools.compiler.config.CompilerConfig;
import dyvilx.tools.repl.DyvilREPL;
import dyvilx.tools.repl.lang.I18n;

public class DebugCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "debug";
	}

	@Override
	public String getUsage()
	{
		return ":debug [on|off|true|false|enable|disable]";
	}

	@Override
	public void execute(DyvilREPL repl, String argument)
	{
		final CompilerConfig config = repl.getCompiler().config;

		final boolean enableDebug;
		if (argument != null)
		{
			switch (argument.toLowerCase())
			{
			case "on":
			case "true":
			case "enable":
				enableDebug = true;
				break;
			case "off":
			case "false":
			case "disable":
				enableDebug = false;
				break;
			default:
				repl.getErrorOutput().println(I18n.get("command.argument.invalid", this.getUsage()));
				return;
			}
		}
		else
		{
			enableDebug = !config.isDebug();
		}

		config.setDebug(enableDebug);
		repl.getOutput().println(I18n.get(enableDebug ? "command.debug.on" : "command.debug.off"));
	}
}
