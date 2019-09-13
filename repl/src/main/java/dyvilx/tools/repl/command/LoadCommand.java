package dyvilx.tools.repl.command;

import dyvilx.tools.repl.DyvilREPL;
import dyvilx.tools.repl.lang.I18n;
import org.jline.builtins.Completers;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

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
	public List<Completers.TreeCompleter.Node> getCompletionNodes()
	{
		return Collections.singletonList(node(":load", node(new Completers.FileNameCompleter())));
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
