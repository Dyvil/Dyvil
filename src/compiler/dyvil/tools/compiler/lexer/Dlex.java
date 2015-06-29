package dyvil.tools.compiler.lexer;

import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.lexer.token.*;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;

import static dyvil.tools.compiler.transform.Tokens.*;
import static dyvil.tools.compiler.util.ParserUtil.*;

public final class Dlex
{
	private Dlex()
	{
	}
	
	public static TokenIterator tokenIterator(String code)
	{
		return new TokenIterator(tokenize(code));
	}
	
	public static IToken tokenize(String code)
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
					type = STRING_2;
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
				if (subtype == MOD_DOTS)
				{
					if (c == '.')
					{
						buf.append(c);
					}
					else
					{
						addToken = true;
					}
				}
				else if (c == '_' || c == '$')
				{
					subtype = MOD_SYMBOL | MOD_LETTER;
					buf.append(c);
				}
				else
				{
					boolean letter = (subtype & MOD_LETTER) != 0;
					boolean symbol = (subtype & MOD_SYMBOL) != 0;
					if (letter)
					{
						if (isIdentifierPart(c))
						{
							subtype = MOD_LETTER;
							buf.append(c);
							continue;
						}
					}
					if (symbol)
					{
						if (isIdentifierSymbol(c))
						{
							subtype = MOD_SYMBOL;
							buf.append(c);
							continue;
						}
					}
					addToken = true;
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
				if (c == '.')
				{
					type = FLOAT;
					buf.append('.');
				}
				else if (c == 'l' || c == 'L')
				{
					type = LONG;
					addToken = true;
					reparse = false;
				}
				else if (subtype == MOD_DEC)
				{
					if (isDigit(c))
					{
						buf.append(c);
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
					if (c == 'b' || isBinDigit(c))
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
					if (isOctDigit(c))
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
					if (c == 'x' || isHexDigit(c))
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
				else if (isDigit(c) || c == 'e')
				{
					buf.append(c);
				}
				else
				{
					addToken = true;
				}
				break;
			case STRING:
				if (c == '"' && buf.length() > 0)
				{
					buf.append('"');
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
			case STRING_2:
				if (c == '"' && (buf.length() > 1 || string))
				{
					if (!string && buf.charAt(0) == '@')
					{
						subtype = STRING_2;
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
						if (buf.length() == 0 || buf.charAt(0) != '@')
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
			case CHAR:
				if (c == '\'' && buf.length() > 0)
				{
					buf.append('\'');
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
			case GENERIC_CALL:
				if (c == '[')
				{
					addToken = true;
					reparse = false;
					break;
				}
				continue;
			}
			
			if (addToken)
			{
				prev = addToken(prev, buf, type | subtype, lineNumber, start);
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
			addToken(prev, buf, type | subtype, lineNumber, start);
		}
		
		return first.getNext();
	}
	
	private static int getMode(char c, String code, int i)
	{
		switch (c)
		{
		case '`':
			return SPECIAL_IDENTIFIER;
		case '#':
			if (code.charAt(i + 1) == '[')
			{
				return GENERIC_CALL;
			}
			return IDENTIFIER | MOD_SYMBOL;
		case '"':
			return STRING;
		case '\'':
			return CHAR;
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
		case '@':
			n = code.charAt(i + 1);
			// @"string"
			if (n == '"')
			{
				return STRING_2;
			}
			return IDENTIFIER | MOD_SYMBOL;
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
			else if (isDigit(n))
			{
				return INT | MOD_OCT;
			}
			return INT;
		case '(':
			return Symbols.OPEN_PARENTHESIS;
		case ')':
			return Symbols.CLOSE_PARENTHESIS;
		case '[':
			return Symbols.OPEN_SQUARE_BRACKET;
		case ']':
			return Symbols.CLOSE_SQUARE_BRACKET;
		case '{':
			return Symbols.OPEN_CURLY_BRACKET;
		case '}':
			return Symbols.CLOSE_CURLY_BRACKET;
		case '.':
			n = code.charAt(i + 1);
			if (n == '.')
			{
				return IDENTIFIER | MOD_DOTS;
			}
			return Symbols.DOT;
		case ';':
			return Symbols.SEMICOLON;
		case ',':
			return Symbols.COMMA;
		}
		if (isDigit(c))
		{
			return INT;
		}
		else if (isIdentifierSymbol(c))
		{
			return IDENTIFIER | MOD_SYMBOL;
		}
		else if (isIdentifierPart(c))
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
		case '$':
			buf.append('$');
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
	
	private static IToken addToken(IToken prev, String s, int type, int line, int start, int len)
	{
		switch (type)
		{
		case IDENTIFIER:
		case LETTER_IDENTIFIER:
		{
			int i = Keywords.getKeywordType(s);
			if (i == 0)
			{
				return new IdentifierToken(prev, Name.get(s), type, line, start, start + len);
			}
			return new KeywordToken(prev, i, line, start, start + len);
		}
		case DOT_IDENTIFIER:
		case SYMBOL_IDENTIFIER:
		case SYMBOL_IDENTIFIER | LETTER_IDENTIFIER:
		{
			int i = Symbols.getSymbolType(s);
			if (i == 0)
			{
				return new IdentifierToken(prev, Name.get(s), type, line, start, start + len);
			}
			return new SymbolToken(prev, i, line, start);
		}
		case SPECIAL_IDENTIFIER:
			return new IdentifierToken(prev, Name.getSpecial(s), type, line, start, start + len);
		case SYMBOL:
		case Symbols.DOT:
		case Symbols.COLON:
		case Symbols.SEMICOLON:
		case Symbols.COMMA:
		case Symbols.EQUALS:
		case Symbols.HASH:
		case Symbols.WILDCARD:
		case Symbols.ARROW_OPERATOR:
			/* Brackets */
		case Symbols.OPEN_BRACKET:
		case Symbols.CLOSE_BRACKET:
		case Symbols.OPEN_PARENTHESIS:
		case Symbols.CLOSE_PARENTHESIS:
		case Symbols.OPEN_SQUARE_BRACKET:
		case Symbols.CLOSE_SQUARE_BRACKET:
		case Symbols.OPEN_CURLY_BRACKET:
		case Symbols.CLOSE_CURLY_BRACKET:
			return new SymbolToken(prev, type, line, start);
		case INT:
			return new IntToken(prev, Integer.parseInt(s, 10), line, start, start + len);
		case INT | MOD_BIN:
			return new IntToken(prev, Integer.parseInt(s.substring(2), 2), line, start, start + len);
		case INT | MOD_OCT:
			return new IntToken(prev, Integer.parseInt(s.substring(1), 8), line, start, start + len);
		case INT | MOD_HEX:
			return new IntToken(prev, Integer.parseInt(s.substring(2), 16), line, start, start + len);
		case LONG:
			return new LongToken(prev, Long.parseLong(s, 10), line, start, start + len);
		case LONG | MOD_BIN:
			return new LongToken(prev, Long.parseLong(s.substring(2), 2), line, start, start + len);
		case LONG | MOD_OCT:
			return new LongToken(prev, Long.parseLong(s.substring(1), 8), line, start, start + len);
		case LONG | MOD_HEX:
			return new LongToken(prev, Long.parseLong(s.substring(2), 16), line, start, start + len);
		case FLOAT:
			return new FloatToken(prev, Float.parseFloat(s), line, start, start + len);
		case FLOAT | MOD_HEX:
			return new FloatToken(prev, Float.parseFloat(s.substring(2)), line, start, start + len);
		case DOUBLE:
			return new DoubleToken(prev, Double.parseDouble(s), line, start, start + len);
		case DOUBLE | MOD_HEX:
			return new DoubleToken(prev, Double.parseDouble(s.substring(2)), line, start, start + len);
		case STRING:
			return new StringToken(prev, STRING, s.substring(1, len - 1), line, start, start + len);
		case STRING_2:
			return new StringToken(prev, STRING, s.substring(2), line, start, start + len);
		case STRING_START:
			return new StringToken(prev, STRING_START, s.substring(2), line, start, start + len);
		case STRING_PART:
			return new StringToken(prev, STRING_PART, s, line, start, start + len);
		case STRING_END:
			return new StringToken(prev, STRING_END, s, line, start, start + len);
		case CHAR:
			return new CharToken(prev, s.charAt(1), line, start);
		case GENERIC_CALL:
			return new SymbolToken(prev, Symbols.GENERIC_CALL, line, start);
		}
		return null;
	}
	
	private static IToken addToken(IToken prev, StringBuilder buf, int type, int line, int start)
	{
		String s = buf.toString();
		int len = buf.length();
		buf.delete(0, len);
		return addToken(prev, s, type, line, start, len);
	}
}
