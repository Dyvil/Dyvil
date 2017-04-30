package dyvil.tools.repl.context;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.io.Console;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.lexer.DyvilLexer;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.source.TextSource;
import dyvil.tools.parsing.token.IToken;

public class Colorizer
{
	public static String colorize(String text, REPLContext context)
	{
		final TextSource source = new TextSource(text);

		final TokenIterator tokens = new DyvilLexer(new MarkerList(Markers.INSTANCE), DyvilSymbols.INSTANCE)
			                             .tokenize(text);

		final List<StringBuilder> lines = new ArrayList<>(source.lineCount());
		for (int i = 0, count = source.lineCount(); i < count; i++)
		{
			lines.add(new StringBuilder(source.getLine(i + 1)));
		}

		// iterate, starting from the last token
		for (IToken token = tokens.last(); token != null && token.type() != Tokens.EOF; token = token.prev())
		{
			final String color = tokenColor(token, context);
			if (color != null)
			{
				//noinspection MismatchedQueryAndUpdateOfStringBuilder
				final StringBuilder line = lines.get(token.startLine() - 1);
				line.insert(token.endColumn(), Console.ANSI_RESET);
				line.insert(token.startColumn(), color);
			}
		}

		return lines.reduce((stringBuilder, s) -> stringBuilder.append('\n').append(s)).toString();
	}

	private static String tokenColor(IToken last, REPLContext context)
	{
		final int type = last.type();
		if ((type & (Tokens.STRING | Tokens.INT | Tokens.LONG | Tokens.FLOAT | Tokens.DOUBLE)) != 0)
		{
			return Console.ANSI_BLUE;
		}
		if ((type & Tokens.SYMBOL) != 0)
		{
			return Console.ANSI_ESCAPE + "[1m"; // BOLD
		}
		if ((type & (Tokens.KEYWORD | Tokens.SYMBOL)) != 0)
		{
			return Console.ANSI_ESCAPE + "[1m"; // BOLD
		}
		if ((type & Tokens.IDENTIFIER) != 0 && context.isMember(last.nameValue()))
		{
			return Console.ANSI_BLUE;
		}
		return null;
	}
}
