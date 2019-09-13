package dyvilx.tools.compiler.parser;

import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

public class BracketMatcher
{
	public static IToken findMatch(IToken token)
	{
		return findMatch(token, false);
	}

	/**
	 * Finds the next matching parenthesis, brace, angle bracket or square bracket.
	 *
	 * @param token
	 * 	the opening parenthesis, brace, angle bracket or square bracket token
	 * @param angleBrackets
	 * 	if angle brackets should be considered as well
	 *
	 * @return the closing parenthesis, brace or bracket token or {@code null}
	 */
	public static IToken findMatch(IToken token, boolean angleBrackets)
	{
		int parenDepth = 0;
		int bracketDepth = 0;
		int braceDepth = 0;
		int angleDepth = 0;

		outer:
		for (; ; token = token.next())
		{
			switch (token.type())
			{
			case Tokens.EOF:
				return null;
			case BaseSymbols.OPEN_PARENTHESIS:
				parenDepth++;
				continue;
			case BaseSymbols.CLOSE_PARENTHESIS:
				if (--parenDepth < 0)
				{
					return null;
				}
				break;
			case BaseSymbols.OPEN_SQUARE_BRACKET:
				bracketDepth++;
				continue;
			case BaseSymbols.CLOSE_SQUARE_BRACKET:
				if (--bracketDepth < 0)
				{
					return null;
				}
				break;
			case BaseSymbols.OPEN_CURLY_BRACKET:
				braceDepth++;
				continue;
			case BaseSymbols.CLOSE_CURLY_BRACKET:
				if (--braceDepth < 0)
				{
					return null;
				}
				break;
			case Tokens.SYMBOL_IDENTIFIER:
			case Tokens.LETTER_IDENTIFIER:
				if (!angleBrackets)
				{
					break;
				}

				final String symbol = token.nameValue().unqualified;
				for (int i = 0, length = symbol.length(); i < length; i++)
				{
					switch (symbol.charAt(i))
					{
					case '<':
						angleDepth++;
						continue;
					case '>':
						if (--angleDepth < 0)
						{
							return null;
						}
						break;
					}
				}
				break;
			case DyvilSymbols.ARROW_LEFT:
				angleDepth++;
				continue;
			case BaseSymbols.SEMICOLON:
				if (angleBrackets && parenDepth == 0 && bracketDepth == 0 && braceDepth == 0)
				{
					return null;
				}
			}

			// No need for angleBrackets check because angleDepth will always be 0 without the flag
			if (parenDepth == 0 && bracketDepth == 0 && braceDepth == 0 && angleDepth == 0)
			{
				return token;
			}
		}
	}
}
