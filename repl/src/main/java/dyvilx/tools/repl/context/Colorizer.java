package dyvilx.tools.repl.context;

import dyvil.io.Console;
import dyvil.source.TextSource;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.TokenList;
import dyvilx.tools.parsing.lexer.DyvilLexer;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvilx.tools.parsing.token.IToken;

public class Colorizer
{
	public static String colorize(String text, REPLContext context)
	{
		final TextSource source = new TextSource(text);

		final TokenList tokens = new DyvilLexer(new MarkerList(Markers.INSTANCE), DyvilSymbols.INSTANCE).tokenize(text);

		// Split into lines
		final int lineCount = source.lineCount();
		final StringBuilder[] lines = new StringBuilder[lineCount];
		for (int i = 0; i < lineCount; i++)
		{
			lines[i] = new StringBuilder(source.line(i + 1));
		}

		// iterate, starting from the last token
		for (IToken token = tokens.last(); token != null && token.type() != Tokens.EOF; token = token.prev())
		{
			final String color = tokenColor(token, context);
			if (color != null)
			{
				// insert ANSI color codes before and after the token
				final StringBuilder line = lines[token.startLine() - 1];
				line.insert(Math.min(line.length(), token.endColumn()), Console.ANSI_RESET);
				line.insert(Math.min(line.length(), token.startColumn()), color);
			}
		}

		// Merge back together
		final StringBuilder first = lines[0];
		for (int i = 1; i < lineCount; i++)
		{
			first.append('\n').append(lines[i]);
		}
		return first.toString();
	}

	private static String tokenColor(IToken token, REPLContext context)
	{
		final int type = token.type();
		if ((type & (Tokens.INT | Tokens.LONG | Tokens.FLOAT | Tokens.DOUBLE)) != 0) // number literals
		{
			return Console.ANSI_BLUE;
		}
		if ((type & (Tokens.STRING | Tokens.VERBATIM_STRING | Tokens.SINGLE_QUOTED_STRING | Tokens.VERBATIM_CHAR))
		    != 0) // string literals
		{
			return Console.ANSI_GREEN + Console.ANSI_ITALIC;
		}
		if ((type & (Tokens.KEYWORD | Tokens.SYMBOL)) != 0) // keywords and symbols
		{
			return Console.ANSI_RED;
		}
		if ((type & Tokens.IDENTIFIER) != 0 && context != null && context.isMember(
			token.nameValue())) // members / identifiers
		{
			return Console.ANSI_YELLOW;
		}
		return null;
	}
}
