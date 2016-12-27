package dyvil.tools.gensrc.ast;

import dyvil.tools.gensrc.GenSrc;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

public class Specializer
{
	private static final int IF_BLOCK  = 1;
	private static final int FOR_BLOCK = 2;

	private final GenSrc         gensrc;
	private final File           sourceFile;
	private final List<String>   lines;
	private final PrintStream    writer;
	private final ReplacementMap replacements;

	public Specializer(GenSrc gensrc, File sourceFile, List<String> lines, PrintStream writer,
		                  ReplacementMap replacements)
	{
		this.gensrc = gensrc;
		this.sourceFile = sourceFile;
		this.lines = lines;
		this.writer = writer;
		this.replacements = replacements;
	}

	public void processLines()
	{
		this.processLines(0, this.lines.size(), new LazyReplacementMap(this.replacements), 0, true, true);
	}

	private int processIf(int start, int end, LazyReplacementMap replacements, boolean outer, boolean condition)
	{
		return this.processLines(start, end, new LazyReplacementMap(replacements), IF_BLOCK, outer, condition);
	}

	private int processLines(int start, int end, LazyReplacementMap replacements, int enclosingBlock,
		                        boolean processOuter, boolean ifCondition)
	{
		boolean hasElse = false;

		for (; start < end; start++)
		{
			final String line = this.lines.get(start);
			final int length = line.length();

			final int hashIndex;
			if (length < 2 || (hashIndex = skipWhitespace(line, 0, length)) >= length || line.charAt(hashIndex) != '#')
			{
				// no leading directive

				if (processOuter && ifCondition)
				{
					this.writer.println(processLine(line, 0, length, replacements));
				}
				continue;
			}

			final int directiveStart = hashIndex + 1;
			final int directiveEnd = findIdentifierEnd(line, directiveStart, length);
			final String directive = line.substring(directiveStart, directiveEnd);
			switch (directive)
			{
			case "if":
			{
				// TODO proper expression handling for #if directives
				final boolean innerCondition = replacements.getBoolean(parseIdentifier(line, directiveEnd, length));

				start = this.processIf(start + 1, end, replacements, processOuter && ifCondition, innerCondition);
				continue;
			}
			case "ifdef":
			{
				// using isDefined instead of getBoolean
				final boolean innerCondition = replacements.isDefined(parseIdentifier(line, directiveEnd, length));

				start = this.processIf(start + 1, end, replacements, processOuter && ifCondition, innerCondition);
				continue;
			}
			case "ifndef":
			{
				// note the '!'
				final boolean innerCondition = !replacements.isDefined(parseIdentifier(line, directiveEnd, length));

				start = this.processIf(start + 1, end, replacements, processOuter && ifCondition, innerCondition);
				continue;
			}
			case "else":
				if (enclosingBlock == IF_BLOCK && !hasElse)
				{
					ifCondition = !ifCondition;
					hasElse = true;
				}
				continue;
			case "endif":
				if (enclosingBlock == IF_BLOCK)
				{
					return start;
				}
				continue;
			case "for":
				if (processOuter && ifCondition)
				{
					final int forEnd = this.processFor(start, end, replacements, line, directiveEnd, length);
					if (forEnd >= 0)
					{
						start = forEnd;
						continue;
					}
				}

				// skip ahead to the line after #endfor
				start = this.processLines(start + 1, end, replacements, FOR_BLOCK, false, false);
				continue;
			case "foreach":
				if (processOuter && ifCondition)
				{
					final int forEnd = this.processForEach(start, end, line, directiveEnd, length, replacements);
					if (forEnd >= 0)
					{
						start = forEnd;
						continue;
					}
				}

				// skip ahead to the line after #endfor
				start = this.processLines(start + 1, end, replacements, FOR_BLOCK, false, false);
				continue;
			case "endfor":
				if (enclosingBlock == FOR_BLOCK)
				{
					return start;
				}
				continue;
			case "end":
				if (enclosingBlock != 0)
				{
					return start;
				}
				continue;
			case "import":
			{
				final Specialization[] specs = this.parseSpecs(line, directiveEnd, length, replacements);
				for (Specialization spec : specs)
				{
					replacements.importFrom(spec);
				}
				continue;
			}
			case "process":
				if (processOuter && ifCondition)
				{
					// process the remainder of the line
					this.writer.println(getProcessedArgument(line, directiveEnd, length, replacements));
				}
				continue;
			case "literal":
				if (processOuter && ifCondition)
				{
					// simply append the remainder of the line verbatim
					final String remainder = getArgument(line, directiveEnd, length);
					this.writer.println(remainder);
				}
				continue;
			case "comment":
				continue;
			case "define":
			{
				if (!processOuter || !ifCondition)
				{
					continue;
				}

				final int keyStart = skipWhitespace(line, directiveEnd, length);
				final int keyEnd = findIdentifierEnd(line, keyStart, length);
				if (keyStart == keyEnd) // missing key
				{
					continue;
				}

				final int valueStart = skipWhitespace(line, keyEnd, length);

				final String key = line.substring(keyStart, keyEnd);
				final String value = line.substring(valueStart, length);
				replacements.define(key, value);
				continue;
			}
			case "undef":
			case "undefine":
			{
				if (!processOuter || !ifCondition)
				{
					continue;
				}
				final String key = parseIdentifier(line, directiveEnd, length);
				if (!key.isEmpty()) // missing key
				{
					replacements.undefine(key);
				}
				continue;
			}
			}

			// TODO invalid directive error/warning
		}

		return end;
	}

	private int processFor(int start, int end, LazyReplacementMap replacements, String line, int directiveEnd,
		                      int length)
	{
		final String[] parts = getProcessedArgument(line, directiveEnd, length, replacements).split("\\s*;\\s*");

		if (parts.length < 3)
		{
			return -1;
		}

		final String varName = parts[0];
		final int from;
		final int to;

		try
		{
			from = Integer.parseInt(parts[1]);
			to = Integer.parseInt(parts[2]);
		}
		catch (NumberFormatException ignored)
		{
			return -1;
		}

		final LazyReplacementMap scope = new LazyReplacementMap(replacements);
		int forEnd = -1;

		for (int i = from; i <= to; i++)
		{
			scope.define(varName, Integer.toString(i));
			forEnd = this.processLines(start + 1, end, scope, FOR_BLOCK, true, true);
		}

		return forEnd;
	}

	private int processForEach(int startLine, int endLine, String line, int start, int end,
		                          LazyReplacementMap replacements)
	{
		final Specialization[] specs = this.parseSpecs(line, start, end, replacements);

		int forEnd = -1;

		// repeat processing the following lines once for each specified specialization
		for (Specialization spec : specs)
		{
			if (spec != null)
			{
				forEnd = this.processLines(start + 1, end, new ForReplacementMap(spec, replacements), FOR_BLOCK, true,
				                           true);
			}
		}
		return forEnd;
	}

	private static String getArgument(String line, int start, int end)
	{
		return line.substring(skipWhitespace(line, start, end));
	}

	private static String getProcessedArgument(String line, int start, int end, LazyReplacementMap replacements)
	{
		return processLine(line, skipWhitespace(line, start, end), end, replacements);
	}

	private Specialization[] parseSpecs(String line, int start, int end, LazyReplacementMap replacements)
	{
		final String[] files = getProcessedArgument(line, start, end, replacements).split("\\s*,\\s*");
		final Specialization[] specs = new Specialization[files.length];
		for (int i = 0; i < files.length; i++)
		{
			specs[i] = Specialization.resolveSpec(this.gensrc, files[i], this.sourceFile);
		}
		return specs;
	}

	private static String parseIdentifier(String line, int start, int end)
	{
		final int keyStart = skipWhitespace(line, start, end);
		final int keyEnd = findIdentifierEnd(line, keyStart, end);
		return line.substring(keyStart, keyEnd);
	}

	private static String processLine(String line, int start, int end, ReplacementMap replacements)
	{
		if (start == end)
		{
			return line;
		}

		final StringBuilder builder = new StringBuilder(end - start);

		int prev = 0;

		for (int i = start; i < end; )
		{
			final char c = line.charAt(i);

			if (c == '#' && i + 1 < end && line.charAt(i + 1) == '#')
			{
				// two consecutive ## are stripped

				// append contents before this identifier
				builder.append(line, prev, i);
				i = prev = i + 2; // advance by two characters
				continue;
			}
			if (!Character.isJavaIdentifierStart(c))
			{
				// advance to the first identifier start character
				i++;
				continue;
			}

			// append contents before this identifier
			builder.append(line, prev, i);

			// index of the first character that is not part of this identifier
			final int nextIndex = findIdentifierEnd(line, i + 1, end);
			final String key = line.substring(i, nextIndex);
			final String replacement = replacements.getReplacement(key);

			if (replacement != null)
			{
				builder.append(replacement); // append the replacement instead of the identifier
			}
			else
			{
				builder.append(line, i, nextIndex); // append the original identifier
			}
			i = prev = nextIndex;
		}

		// append remaining characters on line
		builder.append(line, prev, end);
		return builder.toString();
	}

	private static int findIdentifierEnd(String line, int start, int end)
	{
		for (; start < end; start++)
		{
			if (!Character.isJavaIdentifierPart(line.charAt(start)))
			{
				return start;
			}
		}
		return end;
	}

	private static int findWhitespace(String line, int start, int end)
	{
		for (; start < end; start++)
		{
			if (Character.isWhitespace(line.charAt(start)))
			{
				return start;
			}
		}
		return end;
	}

	/**
	 * Returns the first index greater than or equal to {@code start} where the character in {@code line} is NOT
	 * whitespace. If no such index is found, {@code end} is returned.
	 *
	 * @param line
	 * 	the string to check
	 * @param start
	 * 	the first index (inclusive) to check
	 * @param end
	 * 	the last index (exclusive) to check
	 *
	 * @return the first index {@code >= start} and {@code < end} where the character in the {@code string} is
	 * non-whitespace, or {@code end}.
	 */
	private static int skipWhitespace(String line, int start, int end)
	{
		for (; start < end; start++)
		{
			if (!Character.isWhitespace(line.charAt(start)))
			{
				return start;
			}
		}
		return end;
	}
}
