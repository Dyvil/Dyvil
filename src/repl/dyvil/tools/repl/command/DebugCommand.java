package dyvil.tools.repl.command;

import dyvil.tools.compiler.config.CompilerConfig;
import dyvil.tools.repl.DyvilREPL;

public class DebugCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "debug";
	}
	
	@Override
	public String getDescription()
	{
		return "Enables or disables Debug Mode";
	}

	@Override
	public String getUsage()
	{
		return ":debug [on|off|true|false|enable|disable]";
	}

	@Override
	public void execute(DyvilREPL repl, String... args)
	{
		final CompilerConfig config = repl.getCompiler().config;

		final boolean enableDebug;
		if (args.length > 0)
		{
			switch (args[0].toLowerCase())
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
				repl.getErrorOutput().println("Invalid Argument. Usage: " + this.getUsage());
				return;
			}
		}
		else
		{
			enableDebug = !config.isDebug();
		}

		config.setDebug(enableDebug);
		repl.getOutput().println("Setting debug mode to " + (enableDebug ? "ON" : "OFF"));
	}
}
