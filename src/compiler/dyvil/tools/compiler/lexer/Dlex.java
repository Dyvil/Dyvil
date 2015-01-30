package dyvil.tools.compiler.lexer;

import static dyvil.tools.compiler.util.ParserUtil.*;

import java.util.Iterator;

import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.Token;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class Dlex implements Iterable<IToken>
{
	protected final CodeFile	file;
	protected IToken			first;
	
	public Dlex(CodeFile file)
	{
		this.file = file;
	}
	
	public void tokenize()
	{
		String code = this.file.getCode();
		int len = code.length();
		
		StringBuilder buf = new StringBuilder(20);
		Token first = new Token(-1, "", (byte) 0, null, null, 0, -1, -1);
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
				else if (c <= ' ')
				{
					continue;
				}
				
				int m = getMode(c, code, i);
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
				else
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
		
		this.first = first.next();
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
			else
			{
				return Tokens.TYPE_IDENTIFIER | Tokens.MOD_SYMBOL;
			}
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
	
	private Token addToken(Token prev, String s, int type, int line, int start, int len)
	{
		Token t;
		if ((type & Tokens.TYPE_IDENTIFIER) != 0)
		{
			type = ParserUtil.getKeywordType(s, type);
			t = new Token(0, s, type, s, this.file, line, start, start + len);
		}
		else
		{
			t = new Token(0, s, type, parse(type, s), this.file, line, start, start + len);
		}
		
		prev.setNext(t);
		return t;
	}
	
	private Token addToken(Token prev, StringBuilder buf, int type, int line, int start)
	{
		String s = buf.toString();
		int len = buf.length();
		buf.delete(0, len);
		return this.addToken(prev, s, type, line, start, len);
	}
	
	@Override
	public TokenIterator iterator()
	{
		return new TokenIterator(this.first);
	}
	
	public static class TokenIterator implements Iterator<IToken>
	{
		protected IToken	first;
		protected IToken	next;
		
		public TokenIterator(IToken first)
		{
			this.first = first;
			this.next = first;
		}
		
		public void reset()
		{
			this.next = this.first;
		}
		
		public void jump(IToken next)
		{
			this.next = next;
		}
		
		@Override
		public boolean hasNext()
		{
			return this.next instanceof Token;
		}
		
		@Override
		public IToken next()
		{
			try
			{
				IToken next = this.next;
				this.next = next.next();
				return next;
			}
			catch (SyntaxError ex)
			{
				return null;
			}
		}
		
		@Override
		public void remove()
		{
			try
			{
				IToken prev = this.next.prev();
				IToken next = this.next.next();
				
				prev.setNext(next);
				next.setPrev(prev);
				this.next = next;
			}
			catch (SyntaxError ex)
			{
			}
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder('[');
		
		try
		{
			for (IToken token : this)
			{
				buf.append(token.value());
				buf.append(',');
			}
		}
		catch (SyntaxError ex)
		{
		}
		
		buf.setCharAt(buf.length() - 1, ']');
		
		return buf.toString();
	}
	
	public static Object parse(int type, String value)
	{
		switch (type)
		{
		case Tokens.TYPE_INT:
			return Integer.parseInt(value);
		case Tokens.TYPE_INT | Tokens.MOD_BIN:
			return Integer.parseInt(value.substring(2), 2);
		case Tokens.TYPE_INT | Tokens.MOD_OCT:
			return Integer.parseInt(value, 8);
		case Tokens.TYPE_INT | Tokens.MOD_HEX:
			return Integer.parseInt(value.substring(2), 16);
			
		case Tokens.TYPE_LONG:
			return Long.parseLong(value);
		case Tokens.TYPE_LONG | Tokens.MOD_BIN:
			return Long.parseLong(value.substring(2), 2);
		case Tokens.TYPE_LONG | Tokens.MOD_OCT:
			return Long.parseLong(value, 8);
		case Tokens.TYPE_LONG | Tokens.MOD_HEX:
			return Long.parseLong(value.substring(2), 16);
			
		case Tokens.TYPE_FLOAT:
			return Float.parseFloat(value);
		case Tokens.TYPE_FLOAT | Tokens.MOD_HEX:
			return Float.parseFloat(value.substring(2));
			
		case Tokens.TYPE_DOUBLE:
			return Double.parseDouble(value);
		case Tokens.TYPE_DOUBLE | Tokens.MOD_HEX:
			return Double.parseDouble(value.substring(2));
			
		case Tokens.TYPE_STRING:
			return value.substring(1, value.length() - 1);
		case Tokens.TYPE_STRING_2:
			return value.substring(2, value.length() - 1);
		case Tokens.TYPE_CHAR:
			return Character.valueOf(value.charAt(1));
		}
		return value;
	}
}
