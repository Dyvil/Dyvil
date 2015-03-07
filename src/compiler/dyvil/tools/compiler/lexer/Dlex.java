package dyvil.tools.compiler.lexer;

import static dyvil.tools.compiler.util.ParserUtil.*;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.Token;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class Dlex
{
	private Dlex()
	{
	}
	
	public static TokenIterator tokenIterator(String code, CodeFile file)
	{
		return new TokenIterator(tokenize(code, file));
	}
	
	public static IToken tokenize(String code, CodeFile file)
	{
		int len = code.length();
		
		StringBuilder buf = new StringBuilder(20);
		Token first = new Token(-1, "", (byte) 0, null, file, 0, -1, -1);
		Token prev = first;
		int start = 0;
		int lineNumber = 1;
		int i;
		
		char l = 0;
		char c = 0;
		int type = 0;
		int subtype = 0;
		boolean addToken = false;
		boolean reparse = true;
		boolean string = false;
		for (i = 0; i < len; ++i, l = c)
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
				
				int m = string && l == '}' ? Tokens.TYPE_STRING_2 : getMode(c, code, i);
				type = m & 0xFFFF;
				subtype = m & 0xFFFF0000;
			}
			
			switch (type)
			{
			case Tokens.TYPE_IDENTIFIER:
				if (subtype == Tokens.MOD_DOTS)
				{
					if (c == '.')
					{
						buf.append(c);
					}
					else
					{
						addToken = true;
						reparse = true;
					}
				}
				else if (c == '_' || c == '$' || c == '@')
				{
					subtype = Tokens.MOD_SYMBOL | Tokens.MOD_LETTER;
					buf.append(c);
				}
				else
				{
					boolean letter = (subtype & Tokens.MOD_LETTER) != 0;
					boolean symbol = (subtype & Tokens.MOD_SYMBOL) != 0;
					if (letter)
					{
						if (isIdentifierPart(c))
						{
							subtype = Tokens.MOD_LETTER;
							buf.append(c);
							continue;
						}
					}
					if (symbol)
					{
						if (isIdentifierSymbol(c))
						{
							subtype = Tokens.MOD_SYMBOL;
							buf.append(c);
							continue;
						}
					}
					addToken = true;
				}
				break;
			case Tokens.TYPE_SYMBOL:
				buf.append(c);
				addToken = true;
				reparse = false;
				break;
			case Tokens.TYPE_BRACKET:
				buf.append(c);
				addToken = true;
				reparse = false;
				break;
			case Tokens.TYPE_COMMENT:
				if (subtype == Tokens.MOD_LINE)
				{
					if (c == '\n')
					{
						type = 0;
						continue;
					}
				}
				else if (subtype == Tokens.MOD_BLOCK)
				{
					if (l == '*' && c == '/')
					{
						type = 0;
						continue;
					}
				}
				break;
			case Tokens.TYPE_INT:
			case Tokens.TYPE_LONG:
				if (c == '.')
				{
					type = Tokens.TYPE_FLOAT;
					buf.append('.');
				}
				else if (c == 'l' || c == 'L')
				{
					type = Tokens.TYPE_LONG;
					addToken = true;
					reparse = false;
				}
				else if (subtype == Tokens.MOD_DEC)
				{
					if (isDigit(c))
					{
						buf.append(c);
					}
					else if (c == 'f' || c == 'F')
					{
						type = Tokens.TYPE_FLOAT;
						addToken = true;
						reparse = false;
					}
					else if (c == 'd' || c == 'D')
					{
						type = Tokens.TYPE_FLOAT;
						addToken = true;
						reparse = false;
					}
					else
					{
						addToken = true;
					}
				}
				else if (subtype == Tokens.MOD_BIN)
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
				else if (subtype == Tokens.MOD_OCT)
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
				else if (subtype == Tokens.MOD_HEX)
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
			case Tokens.TYPE_FLOAT:
			case Tokens.TYPE_DOUBLE:
				if (c == 'x')
				{
					subtype = Tokens.MOD_HEX;
					buf.append(c);
				}
				else if (c == 'f' || c == 'F')
				{
					addToken = true;
					reparse = false;
				}
				else if (c == 'd' || c == 'D')
				{
					type = Tokens.TYPE_DOUBLE;
					addToken = true;
					reparse = false;
				}
				else if (isDigit(c) || c == '.' || c == 'e')
				{
					buf.append(c);
				}
				else
				{
					addToken = true;
				}
				break;
			case Tokens.TYPE_STRING:
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
				else
				{
					buf.append(c);
				}
				break;
			case Tokens.TYPE_STRING_2:
				if (c == '"' && (buf.length() > 1 || string))
				{
					buf.append('"');
					string = false;
					addToken = true;
					reparse = false;
					break;
				}
				else if (c == '\\' && appendEscape(buf, code.charAt(i + 1)))
				{
					i++;
					continue;
				}
				else if (c == '$' && code.charAt(i + 1) == '{')
				{
					i++;
					buf.append("${");
					addToken = true;
					string = true;
				}
				else
				{
					buf.append(c);
				}
				break;
			case Tokens.TYPE_CHAR:
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
				else
				{
					buf.append(c);
				}
				break;
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
		
		return first.next();
	}
	
	private static int getMode(char c, String code, int i)
	{
		switch (c)
		{
		case '"':
			return Tokens.TYPE_STRING;
		case '\'':
			return Tokens.TYPE_CHAR;
		case '/':
			char n = code.charAt(i + 1);
			if (n == '*')
			{
				return Tokens.BLOCK_COMMENT;
			}
			else if (n == '/')
			{
				return Tokens.LINE_COMMENT;
			}
			else
			{
				return Tokens.TYPE_IDENTIFIER | Tokens.MOD_SYMBOL;
			}
		case '@':
			n = code.charAt(i + 1);
			// @"string"
			if (n == '"')
			{
				return Tokens.TYPE_STRING_2;
			}
			return Tokens.TYPE_IDENTIFIER | Tokens.MOD_SYMBOL;
		case '0':
			n = code.charAt(i + 1);
			if (n == 'b')
			{
				return Tokens.TYPE_INT | Tokens.MOD_BIN;
			}
			else if (n == 'x')
			{
				return Tokens.TYPE_INT | Tokens.MOD_HEX;
			}
			else if (isDigit(n))
			{
				return Tokens.TYPE_INT | Tokens.MOD_OCT;
			}
			return Tokens.TYPE_INT;
		case '(':
			return Tokens.OPEN_PARENTHESIS;
		case ')':
			return Tokens.CLOSE_PARENTHESIS;
		case '[':
			return Tokens.OPEN_SQUARE_BRACKET;
		case ']':
			return Tokens.CLOSE_SQUARE_BRACKET;
		case '{':
			return Tokens.OPEN_CURLY_BRACKET;
		case '}':
			return Tokens.CLOSE_CURLY_BRACKET;
		case '.':
			n = code.charAt(i + 1);
			if (n == '.')
			{
				return Tokens.TYPE_IDENTIFIER | Tokens.MOD_DOTS;
			}
			return Tokens.DOT;
		case ';':
			return Tokens.SEMICOLON;
		case ',':
			return Tokens.COMMA;
		}
		if (isDigit(c))
		{
			return Tokens.TYPE_INT;
		}
		else if (isIdentifierSymbol(c))
		{
			return Tokens.TYPE_IDENTIFIER | Tokens.MOD_SYMBOL;
		}
		else if (isIdentifierPart(c))
		{
			return Tokens.TYPE_IDENTIFIER | Tokens.MOD_LETTER;
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
	
	private static Token addToken(Token prev, String s, int type, int line, int start, int len)
	{
		Token t;
		if ((type & Tokens.TYPE_IDENTIFIER) != 0)
		{
			type = ParserUtil.getKeywordType(s, type);
			t = new Token(0, s, type, s, prev.file, line, start, start + len);
		}
		else
		{
			t = new Token(0, s, type, parse(type, s), prev.file, line, start, start + len);
		}
		
		prev.setNext(t);
		return t;
	}
	
	private static Token addToken(Token prev, StringBuilder buf, int type, int line, int start)
	{
		String s = buf.toString();
		int len = buf.length();
		buf.delete(0, len);
		return addToken(prev, s, type, line, start, len);
	}
	
	public static Object parse(int type, String value)
	{
		switch (type)
		{
		case Tokens.TYPE_INT:
			return Integer.valueOf(value);
		case Tokens.TYPE_INT | Tokens.MOD_BIN:
			return Integer.valueOf(value.substring(2), 2);
		case Tokens.TYPE_INT | Tokens.MOD_OCT:
			return Integer.valueOf(value, 8);
		case Tokens.TYPE_INT | Tokens.MOD_HEX:
			return Integer.valueOf(value.substring(2), 16);
			
		case Tokens.TYPE_LONG:
			return Long.valueOf(value);
		case Tokens.TYPE_LONG | Tokens.MOD_BIN:
			return Long.valueOf(value.substring(2), 2);
		case Tokens.TYPE_LONG | Tokens.MOD_OCT:
			return Long.valueOf(value, 8);
		case Tokens.TYPE_LONG | Tokens.MOD_HEX:
			return Long.valueOf(value.substring(2), 16);
			
		case Tokens.TYPE_FLOAT:
			return Float.valueOf(value);
		case Tokens.TYPE_FLOAT | Tokens.MOD_HEX:
			return Float.valueOf(value.substring(2));
			
		case Tokens.TYPE_DOUBLE:
			return Double.valueOf(value);
		case Tokens.TYPE_DOUBLE | Tokens.MOD_HEX:
			return Double.valueOf(value.substring(2));
			
		case Tokens.TYPE_STRING:
			return value.substring(1, value.length() - 1);
		case Tokens.TYPE_STRING_2:
			int len = value.length();
			int start = 0;
			int end = len;
			if (len > 2 && value.charAt(0) == '@' && value.charAt(1) == '"')
			{
				start = 2;
			}
			if (len > 0 && value.charAt(len - 1) == '"')
			{
				end = len - 1;
			}
			return value.substring(start, end);
		case Tokens.TYPE_CHAR:
			return Character.valueOf(value.charAt(1));
		}
		return value;
	}
}
