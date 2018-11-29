package dyvilx.tools.repl.command;

import dyvilx.tools.repl.DyvilREPL;
import org.jline.builtins.Completers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public interface ICommand
{
	String getName();

	default String[] getAliases()
	{
		return null;
	}

	String getUsage();

	default Stream<String> nameStream()
	{
		final String[] aliases = this.getAliases();
		if (aliases == null)
		{
			return Stream.of(this.getName());
		}
		else
		{
			return Stream.concat(Stream.of(this.getName()), Arrays.stream(aliases));
		}
	}

	default List<Completers.TreeCompleter.Node> getCompletionNodes()
	{
		final Object[] names = this.nameStream().map(s -> ":" + s).toArray();
		return Collections.singletonList(Completers.TreeCompleter.node(names));
	}

	void execute(DyvilREPL repl, String args);
}
