package dyvilx.tools.repl.context;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.io.Console;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.TokenList;
import dyvilx.tools.parsing.lexer.DyvilLexer;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.TextSource;
import dyvilx.tools.parsing.token.IToken;

public class Colorizer
{
	public static String colorize(String text, REPLContext context)
	{
		final TextSource source = new TextSource(text);

		final TokenList tokens = new DyvilLexer(new MarkerList(Markers.INSTANCE), DyvilSymbols.INSTANCE)
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
