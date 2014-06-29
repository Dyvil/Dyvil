package dyvil.tools.compiler.lexer;

import java.util.Iterator;

import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.Token;

import static dyvil.tools.compiler.lexer.token.IToken.*;

public class Dlex implements Iterable<IToken>, Iterator<IToken>
{
	protected final String	code;
	protected IToken		first;
	protected IToken		current;
	
	public Dlex(String code)
	{
		this.code = code;
	}
	
	public void tokenize()
	{
		String code = this.code;
		int len = code.length();
		
		StringBuilder buf = new StringBuilder(20);
		Token first = new Token(-1, "", (byte) 0, -1, -1);
		Token prev = first;
		int start = 0;
		
		char l = 0;
		char c = 0;
		byte mode = 0;
		boolean addToken = false;
		boolean reparse = true;
		for (int i = 0; i < len; ++i, l = c)
		{
			c = code.charAt(i);
			
			if (mode == 0)
			{
				start = i;
				
				if (isWhitespace(c))
				{
					continue;
				}
				
				mode = getMode(c, code, i);
			}
			
			if (mode == TYPE_IDENTIFIER)
			{
				if (isIdentifierPart(c))
				{
					buf.append(c);
				}
				else
				{
					addToken = true;
				}
			}
			else if (mode == TYPE_SYMBOL)
			{
				if (isSymbol(c))
				{
					buf.append(c);
				}
				else
				{
					addToken = true;
				}
			}
			else if (mode == TYPE_BRACKET)
			{
				buf.append(c);
				addToken = true;
				reparse = false;
			}
			else if (mode == TYPE_LINE_COMMENT)
			{
				if (c == '\n')
				{
					mode = 0;
					continue;
				}
			}
			else if (mode == TYPE_BLOCK_COMMENT)
			{
				if (l == '*' && c == '/')
				{
					mode = 0;
					continue;
				}
			}
			else if (mode == TYPE_INT)
			{
				if (c == 'x')
				{
					mode = TYPE_INT_HEX;
				}
				else if (c == 'b')
				{
					mode = TYPE_INT_BIN;
				}
				else if (c == '.')
				{
					mode = TYPE_FLOAT;
					buf.append('.');
				}
				else if (isDigit(c))
				{
					buf.append(c);
				}
				else if (isIntPart(c))
				{
					addToken = true;
					reparse = false;
				}
				else
				{
					addToken = true;
				}
			}
			else if (mode == TYPE_INT_HEX)
			{
				if (isHexDigit(c))
				{
					buf.append(c);
				}
				else if (c == '.')
				{
					mode = TYPE_FLOAT_HEX;
					buf.append('.');
				}
				else if (isIntPart(c))
				{
					addToken = true;
					reparse = false;
				}
				else
				{
					addToken = true;
				}
			}
			else if (mode == TYPE_INT_BIN)
			{
				if (isBinDigit(c))
				{
					buf.append(c);
				}
				else if (isIntPart(c))
				{
					addToken = true;
					reparse = false;
				}
				else
				{
					addToken = true;
				}
			}
			else if (mode == TYPE_FLOAT)
			{
				if (c == 'x')
				{
					mode = TYPE_FLOAT_HEX;
				}
				else if (isDigit(c) || c == '.' || c == 'e')
				{
					buf.append(c);
				}
				else if (isFloatPart(c))
				{
					addToken = true;
					reparse = false;
				}
				else
				{
					addToken = true;
				}
			}
			else if (mode == TYPE_FLOAT_HEX)
			{
				if (isHexDigit(c) || c == '.')
				{
					buf.append(c);
				}
				else
				{
					addToken = true;
				}
			}
			else if (mode == TYPE_STRING)
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
			else if (mode == TYPE_CHAR)
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
				prev = addToken(prev, buf, mode, start, i);
				addToken = false;
				mode = 0;
				
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
		
		this.first = first.next();
		this.current = this.first;
	}
	
	private static byte getMode(char c, String code, int i)
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
				return TYPE_BLOCK_COMMENT;
			else if (n == '/')
				return TYPE_LINE_COMMENT;
			else
				return TYPE_SYMBOL;
		}
		else if (c == '@')
		{
			char n = code.charAt(i + 1);
			if (n == '"')
				return TYPE_STRING;
			else
				return TYPE_IDENTIFIER;
		}	
		else if (isDigit(c))
		{
			return TYPE_INT;
		}
		else if (isIdentifierPart(c))
		{
			return TYPE_IDENTIFIER;
		}
		else if (isBracket(c))
		{
			return TYPE_BRACKET;
		}
		else if (isSymbol(c))
		{
			return TYPE_SYMBOL;
		}
		return 0;
	}
	
	private static Token addToken(Token prev, String s, byte type, int start, int end)
	{
		Token t = new Token(prev.index() + 1, s, type, start, end);
		prev.setNext(t);
		t.setPrev(prev);
		return t;
	}
	
	private static Token addToken(Token prev, StringBuilder buf, byte type, int start, int end)
	{
		String s = buf.toString();
		buf.delete(0, buf.length());
		return addToken(prev, s, type, start, end);
	}
	
	protected static boolean isWhitespace(char c)
	{
		return c == 0 || c == ' ' || c == '\t' || c == '\n' || c == '\r';
	}
	
	protected static boolean isBracket(char c)
	{
		return c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}' || c == '<' || c == '>';
	}
	
	protected static boolean isSymbol(char c)
	{
		return c >= '!' && c <= '~' && !isBracket(c) && !isDigit(c) && !isIdentifierPart(c);
	}
	
	protected static boolean isDigit(char c)
	{
		return c >= '0' && c <= '9';
	}
	
	protected static boolean isBinDigit(char c)
	{
		return c == '0' || c == '1';
	}
	
	protected static boolean isHexDigit(char c)
	{
		return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
	}
	
	protected static boolean isIntPart(char c)
	{
		return (c == 'l' || c == 'L');
	}
	
	protected static boolean isFloatPart(char c)
	{
		return (c == 'f' || c == 'F') || (c == 'd' || c == 'D');
	}
	
	protected static boolean isLetter(char c)
	{
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}
	
	protected static boolean isIdentifierPart(char c)
	{
		return c == '_' || c == '@' || c == '$' || isLetter(c);
	}
	
	@Override
	public Iterator<IToken> iterator()
	{
		return this;
	}
	
	@Override
	public boolean hasNext()
	{
		return this.current.hasNext();
	}
	
	@Override
	public IToken next()
	{
		try
		{
			IToken current = this.current;
			this.current = current.next();
			return current;
		}
		catch (SyntaxError ex)
		{
			ex.print(System.err, this.code, this.current);
			return null;
		}
	}
	
	@Override
	public void remove()
	{
		try
		{
			IToken prev = this.current.prev();
			IToken next = this.current.next();
			
			prev.setNext(next);
			next.setPrev(prev);
			this.current = next;
		}
		catch (SyntaxError ex)
		{
			ex.print(System.err, this.code, this.current);
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
			
			this.current = this.first;
		}
		catch (SyntaxError ex)
		{
		}
		
		buf.setCharAt(buf.length() - 1, ']');
		
		return buf.toString();
	}
}
