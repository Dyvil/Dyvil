package dyvil.tools.compiler.lexer;

import static dyvil.tools.compiler.lexer.token.IToken.*;
import static dyvil.tools.compiler.util.ParserUtil.*;

import java.util.Iterator;

import dyvil.tools.compiler.Dyvilc;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.Token;

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
			if (c == '\n')
			{
				lineNumber++;
			}
			
			if (type == 0)
			{
				start = i;
				
				if (isWhitespace(c))
				{
					continue;
				}
				
				int m = getMode(c, code, i);
				type = m & 0xFFFF;
				subtype = m & 0xFFFF0000;
			}
			
			if (type == TYPE_IDENTIFIER)
			{
				if (subtype == MOD_DOTS)
				{
					if (c == '.')
					{
						buf.append(c);
					}
				}
				else
				{
					boolean letter = (subtype & MOD_LETTER) != 0;
					boolean symbol = (subtype & MOD_SYMBOL) != 0;
					if (letter)
					{
						if (c == '_' || c == '$' || c == '@')
						{
							subtype |= MOD_SYMBOL;
							buf.append(c);
						}
						else if (isIdentifierPart(c))
						{
							subtype = MOD_LETTER;
							buf.append(c);
						}
						else if (!symbol)
						{
							addToken = true;
						}
					}
					if (symbol)
					{
						if (c == '_' || c == '$' || c == '@')
						{
							subtype |= MOD_LETTER;
							buf.append(c);
						}
						else if (isIdentifierSymbol(c))
						{
							subtype = MOD_SYMBOL;
							buf.append(c);
						}
						else if (!letter)
						{
							addToken = true;
						}
					}
				}
			}
			else if (type == TYPE_SYMBOL)
			{
				buf.append(c);
				addToken = true;
				reparse = false;
			}
			else if (type == TYPE_BRACKET)
			{
				buf.append(c);
				addToken = true;
				reparse = false;
			}
			else if (type == TYPE_COMMENT)
			{
				if (subtype == MOD_LINE)
				{
					if (c == '\n')
					{
						type = 0;
						continue;
					}
				}
				else if (subtype == MOD_BLOCK)
				{
					if (l == '*' && c == '/')
					{
						type = 0;
						continue;
					}
				}
			}
			else if (type == TYPE_INT || type == TYPE_LONG)
			{
				if (c == '.')
				{
					type = TYPE_FLOAT;
					buf.append('.');
				}
				else if (c == 'l' || c == 'L')
				{
					type = TYPE_LONG;
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
						type = TYPE_FLOAT;
						addToken = true;
						reparse = false;
					}
					else if (c == 'd' || c == 'D')
					{
						type = TYPE_FLOAT;
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
			}
			else if (type == TYPE_FLOAT || type == TYPE_DOUBLE)
			{
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
					type = TYPE_DOUBLE;
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
			}
			else if (type == TYPE_STRING)
			{
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
			}
			else if (type == TYPE_CHAR)
			{
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
			}
			
			if (addToken)
			{
				prev = this.addToken(prev, buf, type | subtype, lineNumber, start, i);
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
			this.addToken(prev, buf, type, lineNumber, start, i);
		}
		
		this.first = first.next();
	}
	
	private static int getMode(char c, String code, int i)
	{
		if (c == '"')
		{
			return TYPE_STRING;
		}
		else if (c == '\'')
		{
			return TYPE_CHAR;
		}
		else if (c == '/')
		{
			char n = code.charAt(i + 1);
			if (n == '*')
			{
				return TYPE_BLOCK_COMMENT;
			}
			else if (n == '/')
			{
				return TYPE_LINE_COMMENT;
			}
			else
			{
				return TYPE_SYMBOL;
			}
		}
		else if (c == '@')
		{
			char n = code.charAt(i + 1);
			// @"string"
			if (n == '"')
			{
				return TYPE_STRING_2;
			}
			else if (isWhitespace(c))
			{
				return TYPE_SYMBOL;
			}
			else
			{
				return TYPE_IDENTIFIER;
			}
		}
		else if (c == '0')
		{
			char n = code.charAt(i + 1);
			if (n == 'b')
			{
				return TYPE_INT | MOD_BIN;
			}
			else if (n == 'x')
			{
				return TYPE_INT | MOD_HEX;
			}
			else if (isDigit(n))
			{
				return TYPE_INT | MOD_OCT;
			}
			return TYPE_INT;
		}
		else if (isDigit(c))
		{
			return TYPE_INT;
		}
		else if (isIdentifierSymbol(c))
		{
			return TYPE_IDENTIFIER | MOD_SYMBOL;
		}
		else if (isIdentifierPart(c))
		{
			return TYPE_IDENTIFIER | MOD_LETTER;
		}
		else if (isOpenBracket(c))
		{
			return TYPE_OPEN_BRACKET;
		}
		else if (isCloseBracket(c))
		{
			return TYPE_CLOSE_BRACKET;
		}
		else if (c == '.')
		{
			char n = code.charAt(i + 1);
			if (n == '.')
			{
				return TYPE_IDENTIFIER | MOD_DOTS;
			}
			return TYPE_SYMBOL;
		}
		else if (isSymbol(c))
		{
			return TYPE_SYMBOL;
		}
		return 0;
	}
	
	private Token addToken(Token prev, String s, int type, int line, int start, int end)
	{
		Token t = new Token(prev.index() + 1, s, type, parse(type, s), this.file, line, start, end);
		prev.setNext(t);
		t.setPrev(prev);
		return t;
	}
	
	private Token addToken(Token prev, StringBuilder buf, int type, int line, int start, int end)
	{
		String s = buf.toString();
		buf.delete(0, buf.length());
		return this.addToken(prev, s, type, line, start, end);
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
				ex.log(Dyvilc.logger);
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
				ex.log(Dyvilc.logger);
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
		{}
		
		buf.setCharAt(buf.length() - 1, ']');
		
		return buf.toString();
	}
	
	public static Object parse(int type, String value)
	{
		switch (type)
		{
		case TYPE_IDENTIFIER:
			return value;
		case TYPE_SYMBOL:
			return value;
		case TYPE_BRACKET:
			return value;
			
		case TYPE_INT:
			return Integer.parseInt(value);
		case TYPE_INT | MOD_BIN:
			return Integer.parseInt(value.substring(2), 2);
		case TYPE_INT | MOD_OCT:
			return Integer.parseInt(value, 8);
		case TYPE_INT | MOD_HEX:
			return Integer.parseInt(value.substring(2), 16);
			
		case TYPE_LONG:
			return Long.parseLong(value);
		case TYPE_LONG | MOD_BIN:
			return Long.parseLong(value.substring(2), 2);
		case TYPE_LONG | MOD_OCT:
			return Long.parseLong(value, 8);
		case TYPE_LONG | MOD_HEX:
			return Long.parseLong(value.substring(2), 16);
			
		case TYPE_FLOAT:
			return Float.parseFloat(value);
		case TYPE_FLOAT | MOD_HEX:
			return Float.parseFloat(value.substring(2));
			
		case TYPE_DOUBLE:
			return Double.parseDouble(value);
		case TYPE_DOUBLE | MOD_HEX:
			return Double.parseDouble(value.substring(2));
			
		case TYPE_STRING:
			return value.substring(1, value.length() - 1);
		case TYPE_STRING_2:
			return value.substring(2, value.length() - 1);
		case TYPE_CHAR:
			return Character.valueOf(value.charAt(1));
		}
		return null;
	}
}
