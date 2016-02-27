package dyvil.tools.repl.input;

import java.io.*;

public class InputManager
{
	public static final int ESCAPE        = 1;
	public static final int IDENTIFIER    = 2;
	public static final int DOUBLE_STRING = 4;
	public static final int SINGLE_STRING = 8;

	private final PrintStream    output;
	private final BufferedReader input;

	public InputManager(PrintStream output, InputStream input)
	{
		this.output = output;
		this.input = new BufferedReader(new InputStreamReader(input));
	}

	public String readInput() throws IOException
	{
		final StringBuilder buffer = new StringBuilder();

		int braceDepth = 0;
		int parenDepth = 0;
		int bracketDepth = 0;
		byte mode = 0;

		while (true)
		{
			final String input = this.input.readLine();

			if (input == null)
			{
				if (buffer.length() > 0)
				{
					continue;
				}

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
					if (mode >= DOUBLE_STRING)
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

				mode &= ~1;
			}

			buffer.append('\n');
			if (mode == 0 && braceDepth + parenDepth + bracketDepth <= 0)
			{
				break;
			}

			this.printIndent(braceDepth);
		}

		return buffer.toString();
	}

	private void printIndent(int indent)
	{
		this.output.print("| ");
		for (int j = 0; j < indent; j++)
		{
			this.output.print("    "); // 4 spaces
		}
	}
}
