package dyvilx.tools.repl.input;

import dyvilx.tools.repl.DyvilREPL;
import dyvilx.tools.repl.context.Colorizer;
import org.jline.builtins.Completers;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class InputManager
{
	public static final int ESCAPE        = 1;
	public static final int IDENTIFIER    = 2;
	public static final int DOUBLE_STRING = 4;
	public static final int SINGLE_STRING = 8;

	private final LineReader reader;

	public InputManager() throws IOException
	{
		this.reader = createLineReader();
	}

	private static LineReader createLineReader() throws IOException
	{
		return LineReaderBuilder.builder().appName("Dyvil REPL").terminal(createTerminal())
		                        .highlighter(createHighlighter()).completer(createCompleter()).parser(createParser())
		                        .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
		                        .option(LineReader.Option.INSERT_TAB, true).build();
	}

	private static Parser createParser()
	{
		return new DefaultParser()
		{
			@Override
			public ParsedLine parse(String line, int cursor, ParseContext context)
			{
				if (context != ParseContext.COMPLETE && !isValid(line, cursor))
				{
					throw new EOFError(-1, -1, "EOF");
				}

				return super.parse(line, cursor, context);
			}
		};
	}

	private static Highlighter createHighlighter()
	{
		return (reader, buffer) -> AttributedString.fromAnsi(Colorizer.colorize(buffer, null));
	}

	private static Completer createCompleter()
	{
		final List<Completers.TreeCompleter.Node> nodes = DyvilREPL.getCommands().values().stream()
		                                                           .flatMap(c -> c.getCompletionNodes().stream())
		                                                           .collect(Collectors.toList());

		return new Completers.TreeCompleter(nodes);
	}

	private static Terminal createTerminal() throws IOException
	{
		return TerminalBuilder.terminal();
	}

	public String readInput()
	{
		try
		{
			return this.reader.readLine("> ");
		}
		catch (EndOfFileException | UserInterruptException ex)
		{
			return null;
		}
	}

	private static boolean isValid(String line, int cursor)
	{
		int braceDepth = 0;
		int parenDepth = 0;
		int bracketDepth = 0;
		byte mode = 0;

		outer:
		for (int i = 0; i < cursor; i++)
		{
			final char c = line.charAt(i);

			switch (c)
			{
			case '`':
				if (mode == 0 || mode == IDENTIFIER)
				{
					mode ^= IDENTIFIER;
				}
				break;
			case '"':
				if (mode == 0 || mode == DOUBLE_STRING)
				{
					mode ^= DOUBLE_STRING;
				}
				break;
			case '\'':
				if (mode == 0 || mode == SINGLE_STRING)
				{
					mode ^= SINGLE_STRING;
				}
				break;
			case '\\':
				if ((mode & ESCAPE) != 0)
				{
					break;
				}
				if (mode == DOUBLE_STRING || mode == SINGLE_STRING)
				{
					mode |= ESCAPE;
				}
				continue outer;
			case '{':
				if (mode == 0)
				{
					braceDepth++;
				}
				break;
			case '}':
				if (mode == 0)
				{
					braceDepth--;
				}
				break;
			case '(':
				if (mode == 0)
				{
					parenDepth++;
				}
				break;
			case ')':
				if (mode == 0)
				{
					parenDepth--;
				}
				break;
			case '[':
				if (mode == 0)
				{
					bracketDepth++;
				}
				break;
			case ']':
				if (mode == 0)
				{
					bracketDepth--;
				}
				break;
			}

			mode &= ~ESCAPE;
		}

		return mode == 0 && braceDepth + parenDepth + bracketDepth <= 0;
	}
}
