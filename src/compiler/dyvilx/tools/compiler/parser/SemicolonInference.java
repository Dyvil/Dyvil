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
			inferSemicolon(prev, next);
			prev = next;
			next = next.next();
		}
	}
	
	private static void inferSemicolon(IToken prev, IToken next)
	{
		final int prevLine = prev.endLine();
		final int nextLine = next.startLine();
		if (nextLine == prevLine)
		{
			// Don't infer a semicolon
			return;
		}
		
		// Only check tokens if the two tokens are on adjacent lines. Always
		// infer a semicolon if there are one or more blank lines in between
		if (nextLine == prevLine + 1)
		{
			// Check last token on line in question
			final int prevType = prev.type();

			// Check if the previous token is a symbol
			if ((prevType & Tokens.SYMBOL) != 0)
			{
				switch (prevType)
				{
				case DyvilSymbols.HASH:
				case DyvilSymbols.UNDERSCORE:
				case DyvilSymbols.ELLIPSIS:
					break; // continue inference checking
				default:
					return; // don't infer a semicolon
				}
			}
			// Check if the previous token is a keyword, but not a value keyword
			else if ((prevType & Tokens.KEYWORD) != 0)
			{
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
					return; // don't infer a semicolon
				}
			}
			else
			{
				// Check for other token types
				switch (prevType)
				{
				case Tokens.STRING_START:
				case Tokens.STRING_PART:
				case BaseSymbols.OPEN_PARENTHESIS:
				case BaseSymbols.OPEN_SQUARE_BRACKET:
				case BaseSymbols.OPEN_CURLY_BRACKET:
					return;
				}
			}

			// Check first token on the next line

			final int nextType = next.type();
			// Check if the first token on the next line is a symbol
			if ((nextType & Tokens.SYMBOL) != 0)
			{
				switch (nextType)
				{
				case DyvilSymbols.AT:
				case DyvilSymbols.HASH:
					break; // continue inference checking
				default:
					return; // don't infer a semicolon
				}
			}

			// Check for other token types
			switch (nextType)
			{
			case Tokens.STRING_PART:
			case Tokens.STRING_END:
			case BaseSymbols.OPEN_CURLY_BRACKET:
			case BaseSymbols.CLOSE_PARENTHESIS:
			case BaseSymbols.CLOSE_SQUARE_BRACKET:
			case BaseSymbols.CLOSE_CURLY_BRACKET:
				return;
			}
		}
		
		final IToken semicolon = new InferredSemicolon(prevLine, prev.endColumn());
		semicolon.setNext(next);
		semicolon.setPrev(prev);
		next.setPrev(semicolon);
		prev.setNext(semicolon);
	}
}
