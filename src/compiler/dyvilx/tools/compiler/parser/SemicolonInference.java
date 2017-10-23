package dyvilx.tools.compiler.parser;

import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;
import dyvilx.tools.parsing.token.InferredSemicolon;

public final class SemicolonInference
{
	private SemicolonInference()
	{
		// no instances
	}

	public static void inferSemicolons(IToken first)
	{
		if (first == null)
		{
			return;
		}

		IToken prev = first;
		IToken next = first.next();
		while (next.type() != Tokens.EOF)
		{
			next.setPrev(prev);
			if (inferSemicolon(prev, next))
			{
				final IToken semicolon = new InferredSemicolon(prev.endLine(), prev.endColumn());
				semicolon.setNext(next);
				semicolon.setPrev(prev);
				next.setPrev(semicolon);
				prev.setNext(semicolon);
			}
			prev = next;
			next = next.next();
		}
	}

	private static boolean inferSemicolon(IToken prev, IToken next)
	{
		final int prevLine = prev.endLine();
		final int nextLine = next.startLine();
		if (nextLine == prevLine)
		{
			// Obviously never infer a semicolon between tokens on the same line
			return false;
		}

		if (nextLine != prevLine + 1)
		{
			// Always infer a semicolon if there are one or more blank lines in between
			return true;
		}

		// Check last token on line in question
		final int prevType = prev.type();

		if ((prevType & Tokens.KEYWORD) == 0)
		{
			// Don't infer a semicolon for any of the following token types:
			switch (prevType)
			{
			case BaseSymbols.DOT:
			case BaseSymbols.COMMA:
			case BaseSymbols.SEMICOLON:
			case Tokens.STRING_START:
			case Tokens.STRING_PART:
			case BaseSymbols.OPEN_PARENTHESIS:
			case BaseSymbols.OPEN_SQUARE_BRACKET:
			case BaseSymbols.OPEN_CURLY_BRACKET:
				return false;
			}
		}
		else
		{
			// Infer a semicolon if the previous token is a keyword, but not a value keyword
			switch (prevType)
			{
			case DyvilKeywords.TRUE:
			case DyvilKeywords.FALSE:
			case DyvilKeywords.BREAK:
			case DyvilKeywords.CONTINUE:
			case DyvilKeywords.RETURN:
			case DyvilKeywords.THIS:
			case DyvilKeywords.SUPER:
			case DyvilKeywords.NIL:
			case DyvilKeywords.NULL:
				break; // continue inference checking
			default:
				return false;
			}
		}

		// Check first token on the next line

		final int nextType = next.type();

		// Check for other token types
		switch (nextType)
		{
		case BaseSymbols.DOT:
		case BaseSymbols.COMMA:
		case BaseSymbols.SEMICOLON:
		case BaseSymbols.EQUALS:
		case BaseSymbols.COLON:
		case BaseSymbols.OPEN_CURLY_BRACKET:
		case BaseSymbols.CLOSE_PARENTHESIS:
		case BaseSymbols.CLOSE_SQUARE_BRACKET:
		case BaseSymbols.CLOSE_CURLY_BRACKET:
		case Tokens.STRING_PART:
		case Tokens.STRING_END:
			return false;
		}
		return true;
	}
}
