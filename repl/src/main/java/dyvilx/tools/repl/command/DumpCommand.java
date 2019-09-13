package dyvilx.tools.repl.command;

import dyvilx.tools.repl.DyvilREPL;
import dyvilx.tools.repl.lang.I18n;
import org.jline.builtins.Completers;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

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
	public List<Completers.TreeCompleter.Node> getCompletionNodes()
	{
		return Collections.singletonList(node(":dump", node(new Completers.FileNameCompleter())));
	}

	@Override
	public void execute(DyvilREPL repl, String argument)
	{
		if (argument == null)
		{
			repl.getOutput().println(I18n.get("command.dump.disabled"));
			repl.getConfig().setDumpDir(null);
			return;
		}

		File dumpDir = new File(argument);
		repl.getConfig().setDumpDir(dumpDir);

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
