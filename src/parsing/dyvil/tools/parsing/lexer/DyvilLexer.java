package dyvil.tools.parsing.lexer;

import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.marker.SyntaxError;
import dyvil.tools.parsing.token.*;

import static dyvil.tools.parsing.lexer.Tokens.*;

public final class DyvilLexer
{
	private MarkerList	markers;
	private Symbols		symbols;
	
	public DyvilLexer(MarkerList markers, Symbols symbols)
	{
		this.markers = markers;
		this.symbols = symbols;
	}
	
	public TokenIterator tokenize(String code)
	{
		int len = code.length();
		
		StringBuilder buf = new StringBuilder(20);
		IToken first = new InferredSemicolon(0, 0);
		IToken prev = first;
		int start = 0;
		int lineNumber = 1;
		
		char l = 0;
		char c = 0;
		int type = 0;
		int subtype = 0;
		boolean addToken = false;
		boolean reparse = true;
		boolean string = false;
		for (int i = 0; i < len; ++i, l = c)
		{
			c = code.charAt(i);
			
			if (type == 0)
			{
				start = i;
				
				if (c == '\n')
				{
					lineNumber++;
					continue;
				}
				if (c <= ' ')
				{
					continue;
				}
				
				if (string && c == ')')
				{
					type = STRING;
					subtype = STRING_PART;
					continue;
				}
				
				int m = getMode(c, code, i);
				type = m & 0xFFFF;
				subtype = m & 0xFFFF0000;
			}
			
			typeswitch:
			switch (type)
			{
			case IDENTIFIER:
				switch (subtype)
				{
				case MOD_LETTER:
					if (c == '_' || c == '$')
					{
						buf.append(c);
						subtype = MOD_LETTER | MOD_SYMBOL;
						continue;
					}
					if (LexerUtil.isIdentifierPart(c))
					{
						buf.append(c);
						continue;
					}
					addToken = true;
					break typeswitch;
				case MOD_SYMBOL:
					if (c == '_' || c == '$')
					{
						buf.append(c);
						subtype = MOD_LETTER | MOD_SYMBOL;
						continue;
					}
					if (LexerUtil.isIdentifierSymbol(c))
					{
						buf.append(c);
						continue;
					}
					addToken = true;
					break typeswitch;
				case MOD_LETTER | MOD_SYMBOL:
					if (c == '_' || c == '$')
					{
						buf.append(c);
						continue;
					}
					if (LexerUtil.isIdentifierPart(c))
					{
						buf.append(c);
						subtype = MOD_LETTER;
						continue;
					}
					if (LexerUtil.isIdentifierSymbol(c))
					{
						buf.append(c);
						subtype = MOD_LETTER;
						continue;
					}
					addToken = true;
					break typeswitch;
				}
				break;
			case SPECIAL_IDENTIFIER:
				switch (c)
				{
				case '\n':
				case '\t':
				case '\b':
					continue;
				case '`':
					if (buf.length() == 0)
					{
						continue;
					}
					
					addToken = true;
					reparse = false;
					break typeswitch;
				default:
					buf.append(c);
					continue;
				}
			case SYMBOL:
				buf.append(c);
				addToken = true;
				reparse = false;
				break;
			case BRACKET:
				buf.append(c);
				addToken = true;
				reparse = false;
				break;
			case COMMENT:
				if (subtype == MOD_LINE)
				{
					if (c == '\n')
					{
						type = 0;
						lineNumber++;
						continue;
					}
				}
				else if (subtype == MOD_BLOCK)
				{
					if (c == '\n')
					{
						lineNumber++;
					}
					else if (l == '*' && c == '/')
					{
						type = 0;
						continue;
					}
				}
				break;
			case INT:
			case LONG:
				switch (c)
				{
				case '.':
				case 'e':
					type = FLOAT;
					buf.append(c);
					continue;
				case 'l':
				case 'L':
					type = LONG;
					addToken = true;
					reparse = false;
					break;
				case '_':
					continue;
				case '-':
					if (buf.length() == 0)
					{
						buf.append('-');
						continue;
					}
					
					addToken = true;
					reparse = true;
					break;
				}
				if (subtype == MOD_DEC)
				{
					if (LexerUtil.isDigit(c))
					{
						buf.append(c);
					}
					else if (c == 'e' || c == 'E')
					{
						type = FLOAT;
						buf.append('e');
					}
					else if (c == 'f' || c == 'F')
					{
						type = FLOAT;
						addToken = true;
						reparse = false;
					}
					else if (c == 'd' || c == 'D')
					{
						type = DOUBLE;
						addToken = true;
						reparse = false;
					}
					else
					{
						addToken = true;
					}
				}
				else if (subtype == MOD_BIN)
				{
					if (c == 'b' || LexerUtil.isBinDigit(c))
					{
						buf.append(c);
					}
					else
					{
						addToken = true;
					}
				}
				else if (subtype == MOD_OCT)
				{
					if (c == 'o' || LexerUtil.isOctDigit(c))
					{
						buf.append(c);
					}
					else
					{
						addToken = true;
					}
				}
				else if (subtype == MOD_HEX)
				{
					if (c == 'x' || LexerUtil.isHexDigit(c))
					{
						buf.append(c);
					}
					else
					{
						addToken = true;
					}
				}
				break;
			case FLOAT:
			case DOUBLE:
				if (c == 'x')
				{
					subtype = MOD_HEX;
					buf.append(c);
				}
				else if (c == 'f' || c == 'F')
				{
					addToken = true;
					reparse = false;
				}
				else if (c == 'd' || c == 'D')
				{
					type = DOUBLE;
					addToken = true;
					reparse = false;
				}
				else if (LexerUtil.isDigit(c) || c == 'e')
				{
					buf.append(c);
				}
				else
				{
					addToken = true;
				}
				break;
			case STRING:
				if (c == '"' && (buf.length() > 1 || string))
				{
					if (!string && buf.charAt(0) == '"')
					{
						subtype = STRING;
					}
					else
					{
						subtype = STRING_END;
					}
					string = false;
					addToken = true;
					reparse = false;
					break;
				}
				else if (c == '\\')
				{
					char c1 = code.charAt(i + 1);
					if (c1 == '(')
					{
						i += 2;
						if (buf.length() == 0 || buf.charAt(0) != '"')
						{
							subtype = STRING_PART;
						}
						else
						{
							subtype = STRING_START;
						}
						addToken = true;
						string = true;
						break;
					}
					else if (appendEscape(buf, c1))
					{
						i++;
						continue;
					}
					buf.append('\\');
					break;
				}
				else if (c != '\t')
				{
					buf.append(c);
				}
				break;
			case SINGLE_QUOTED_STRING:
				if (c == '\'' && buf.length() > 0)
				{
					addToken = true;
					reparse = false;
				}
				else if (c == '\\' && appendEscape(buf, code.charAt(i + 1)))
				{
					i++;
					continue;
				}
				else if (c != '\t')
				{
					buf.append(c);
				}
				break;
			}
			
			if (addToken)
			{
				prev = this.addToken(prev, buf, type | subtype, lineNumber, start);
				addToken = false;
				type = 0;
				
				if (reparse)
				{
					i--;
				}
				else
				{
					reparse = true;
				}
			}
		}
		
		if (buf.length() > 0)
		{
			this.addToken(prev, buf, type | subtype, lineNumber, start);
		}
		
		return new TokenIterator(first.next());
	}
	
	private static int getMode(char c, String code, int i)
	{
		switch (c)
		{
		case '`':
			return SPECIAL_IDENTIFIER;
		case '"':
			return STRING;
		case '\'':
			return SINGLE_QUOTED_STRING;
		case '/':
			char n = code.charAt(i + 1);
			if (n == '*')
			{
				return BLOCK_COMMENT;
			}
			else if (n == '/')
			{
				return LINE_COMMENT;
			}
			else
			{
				return IDENTIFIER | MOD_SYMBOL;
			}
		case '0':
			n = code.charAt(i + 1);
			if (n == 'b')
			{
				return INT | MOD_BIN;
			}
			else if (n == 'x')
			{
				return INT | MOD_HEX;
			}
			else if (n == 'o')
			{
				return INT | MOD_OCT;
			}
			return INT;
		case '(':
			return BaseSymbols.OPEN_PARENTHESIS;
		case ')':
			return BaseSymbols.CLOSE_PARENTHESIS;
		case '[':
			return BaseSymbols.OPEN_SQUARE_BRACKET;
		case ']':
			return BaseSymbols.CLOSE_SQUARE_BRACKET;
		case '{':
			return BaseSymbols.OPEN_CURLY_BRACKET;
		case '}':
			return BaseSymbols.CLOSE_CURLY_BRACKET;
		case '.':
			n = code.charAt(i + 1);
			if (LexerUtil.isIdentifierSymbol(n))
			{
				return IDENTIFIER | MOD_SYMBOL;
			}
			return BaseSymbols.DOT;
		case ';':
			return BaseSymbols.SEMICOLON;
		case ',':
			return BaseSymbols.COMMA;
		case '_':
		case '$':
			return IDENTIFIER | MOD_SYMBOL | MOD_LETTER;
		case '-':
			if (LexerUtil.isDigit(code.charAt(i + 1)))
			{
				return INT;
			}
			return IDENTIFIER | MOD_SYMBOL;
		}
		if (LexerUtil.isDigit(c))
		{
			return INT;
		}
		else if (LexerUtil.isIdentifierSymbol(c))
		{
			return IDENTIFIER | MOD_SYMBOL;
		}
		else if (LexerUtil.isIdentifierPart(c))
		{
			return IDENTIFIER | MOD_LETTER;
		}
		return 0;
	}
	
	private static boolean appendEscape(StringBuilder buf, char n)
	{
		switch (n)
		{
		case '"':
		case '\'':
		case '\\':
			buf.append(n);
			return true;
		case 'n':
			buf.append('\n');
			return true;
		case 't':
			buf.append('\t');
			return true;
		case 'r':
			buf.append('\r');
			return true;
		case 'b':
			buf.append('\b');
			return true;
		case 'f':
			buf.append('\f');
			return true;
		}
		return false;
	}
	
	private IToken addToken(IToken prev, String s, int type, int line, int start, int len)
	{
		switch (type)
		{
		case IDENTIFIER:
		case LETTER_IDENTIFIER:
		{
			int i = this.symbols.getKeywordType(s);
			if (i == 0)
			{
				return new IdentifierToken(prev, Name.get(s), type, line, start, start + len);
			}
			return new SymbolToken(this.symbols, prev, i, line, start);
		}
		case SYMBOL_IDENTIFIER:
		case SYMBOL_IDENTIFIER | LETTER_IDENTIFIER:
		{
			int i = this.symbols.getSymbolType(s);
			if (i == 0)
			{
				return new IdentifierToken(prev, Name.get(s), type, line, start, start + len);
			}
			return new SymbolToken(this.symbols, prev, i, line, start);
		}
		case SPECIAL_IDENTIFIER:
			return new IdentifierToken(prev, Name.getSpecial(s), type, line, start, start + len);
		case SYMBOL:
		case BaseSymbols.DOT:
		case BaseSymbols.COLON:
		case BaseSymbols.SEMICOLON:
		case BaseSymbols.COMMA:
		case BaseSymbols.EQUALS:
			/* Brackets */
		case BaseSymbols.OPEN_BRACKET:
		case BaseSymbols.CLOSE_BRACKET:
		case BaseSymbols.OPEN_PARENTHESIS:
		case BaseSymbols.CLOSE_PARENTHESIS:
		case BaseSymbols.OPEN_SQUARE_BRACKET:
		case BaseSymbols.CLOSE_SQUARE_BRACKET:
		case BaseSymbols.OPEN_CURLY_BRACKET:
		case BaseSymbols.CLOSE_CURLY_BRACKET:
			return new SymbolToken(BaseSymbols.INSTANCE, prev, type, line, start);
		case INT:
			return this.intToken(prev, s, line, start, len, 10, false);
		case INT | MOD_BIN:
			return this.intToken(prev, s, line, start, len, 2, false);
		case INT | MOD_OCT:
			return this.intToken(prev, s, line, start, len, 8, false);
		case INT | MOD_HEX:
			return this.intToken(prev, s, line, start, len, 16, false);
		case LONG:
			return this.intToken(prev, s, line, start, len, 10, true);
		case LONG | MOD_BIN:
			return this.intToken(prev, s, line, start, len, 2, true);
		case LONG | MOD_OCT:
			return this.intToken(prev, s, line, start, len, 8, true);
		case LONG | MOD_HEX:
			return this.intToken(prev, s, line, start, len, 16, true);
		case FLOAT:
			return new FloatToken(prev, Float.parseFloat(s), line, start, start + len);
		case FLOAT | MOD_HEX:
			return new FloatToken(prev, Float.parseFloat(s.substring(2)), line, start, start + len);
		case DOUBLE:
			return new DoubleToken(prev, Double.parseDouble(s), line, start, start + len);
		case DOUBLE | MOD_HEX:
			return new DoubleToken(prev, Double.parseDouble(s.substring(2)), line, start, start + len);
		case STRING:
			return new StringToken(prev, STRING, s.substring(1), line, start, start + len);
		case STRING_START:
			return new StringToken(prev, STRING_START, s.substring(1), line, start, start + len);
		case STRING_PART:
			return new StringToken(prev, STRING_PART, s, line, start, start + len);
		case STRING_END:
			return new StringToken(prev, STRING_END, s, line, start, start + len);
		case SINGLE_QUOTED_STRING:
			return new StringToken(prev, SINGLE_QUOTED_STRING, s.substring(1), line, start, start + len);
		}
		return null;
	}
	
	private IToken intToken(IToken prev, String s, int line, int start, int len, int radix, boolean isLong)
	{
		IToken token = isLong ? new LongToken(prev, line, start, start + len) : new IntToken(prev, line, start, start + len);
		this.parseInteger(token, s, radix, isLong);
		return token;
	}
	
	private void parseInteger(IToken token, String str, int radix, boolean isLong)
	{
		long result = 0;
		boolean negative = false;
		int from = radix != 10 ? 2 : 0;
		int to = str.length();
		long limit = isLong ? -Long.MAX_VALUE : -Integer.MAX_VALUE;
		long multmin;
		int digit;
		
		char firstChar = str.charAt(from);
		if (firstChar < '0')
		{ // Possible leading "+" or "-"
			if (firstChar == '-')
			{
				negative = true;
				limit = Long.MIN_VALUE;
			}
			from++;
		}
		
		multmin = limit / radix;
		while (from < to)
		{
			// Accumulating negatively avoids surprises near MAX_VALUE
			digit = Character.digit(str.charAt(from++), radix);
			if (result < multmin || (result *= radix) < limit + digit)
			{
				this.markers.add(new SyntaxError(token, "Invalid Integer - Out of Range"));
				return;
			}
			result -= digit;
		}
		
		token.setLong(negative ? result : -result);
	}
	
	private IToken addToken(IToken prev, StringBuilder buf, int type, int line, int start)
	{
		String s = buf.toString();
		int len = buf.length();
		buf.delete(0, len);
		return this.addToken(prev, s, type, line, start, len);
	}
}
