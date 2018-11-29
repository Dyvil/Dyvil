package dyvilx.tools.repl.input;

import dyvil.lang.Strings;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

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
		return LineReaderBuilder.builder().appName("Dyvil REPL").terminal(createTerminal()).build();
	}

	private static Terminal createTerminal() throws IOException
	{
		return TerminalBuilder.terminal();
	}

	public String readInput()
	{
		final StringBuilder buffer = new StringBuilder();

		int braceDepth = 0;
		int parenDepth = 0;
		int bracketDepth = 0;
		byte mode = 0;
		String prompt = "> ";
		String input;

		while (true)
		{
			try
			{
				input = this.reader.readLine(prompt);
			}
			catch (EndOfFileException | UserInterruptException ex)
			{
				return null;
			}

			final int len = input.length();

			outer:
			for (int i = 0; i < len; i++)
			{
				final char c = input.charAt(i);

				buffer.append(c);

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

			buffer.append('\n');
			if (mode == 0 && braceDepth + parenDepth + bracketDepth <= 0)
			{
				break;
			}

			prompt = "| " + Strings.repeat("    ", braceDepth); // 4 spaces
		}

		return buffer.toString();
	}
}
