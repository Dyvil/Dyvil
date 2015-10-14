package dyvil.tools.parsing.lexer;

import java.util.HashMap;
import java.util.Map;

public class BaseSymbols implements Symbols
{
	public static Map<Character, String>	symbolMap		= new HashMap();
	public static Map<String, Character>	replacementMap	= new HashMap();
	
	static
	{
		addReplacement('=', "eq");
		addReplacement('>', "gt");
		addReplacement('<', "lt");
		addReplacement('+', "plus");
		addReplacement('-', "minus");
		addReplacement('*', "times");
		addReplacement('/', "div");
		addReplacement('!', "bang");
		addReplacement('@', "at");
		addReplacement('#', "hash");
		addReplacement('%', "percent");
		addReplacement('^', "up");
		addReplacement('&', "amp");
		addReplacement('~', "tilde");
		addReplacement('?', "qmark");
		addReplacement('|', "bar");
		addReplacement('\\', "bslash");
		addReplacement(':', "colon");
		addReplacement('.', "dot");
	}
	
	public static final BaseSymbols	INSTANCE				= new BaseSymbols();
	
	public static final int			PARENTHESIS				= Tokens.BRACKET | 0x00010000;
	public static final int			SQUARE					= Tokens.BRACKET | 0x00020000;
	public static final int			CURLY					= Tokens.BRACKET | 0x00040000;
	
	public static final int			OPEN					= 0x00000000;
	public static final int			CLOSE					= 0x00100000;
	
	public static final int			OPEN_BRACKET			= Tokens.BRACKET | OPEN;
	public static final int			CLOSE_BRACKET			= Tokens.BRACKET | CLOSE;
	public static final int			OPEN_PARENTHESIS		= PARENTHESIS | OPEN;
	public static final int			CLOSE_PARENTHESIS		= PARENTHESIS | CLOSE;
	public static final int			OPEN_SQUARE_BRACKET		= SQUARE | OPEN;
	public static final int			CLOSE_SQUARE_BRACKET	= SQUARE | CLOSE;
	public static final int			OPEN_CURLY_BRACKET		= CURLY | OPEN;
	public static final int			CLOSE_CURLY_BRACKET		= CURLY | CLOSE;
	
	public static final int			DOT						= Tokens.SYMBOL | 0x00010000;
	public static final int			COLON					= Tokens.SYMBOL | 0x00020000;
	public static final int			SEMICOLON				= Tokens.SYMBOL | 0x00030000;
	public static final int			COMMA					= Tokens.SYMBOL | 0x00040000;
	public static final int			EQUALS					= Tokens.SYMBOL | 0x00050000;
	
	@Override
	public int getKeywordType(String value)
	{
		return 0;
	}
	
	@Override
	public int getSymbolType(String value)
	{
		switch (value)
		{
		case ":":
			return BaseSymbols.COLON;
		case "=":
			return BaseSymbols.EQUALS;
		}
		return 0;
	}
	
	@Override
	public String toString(int type)
	{
		switch (type)
		{
		case BaseSymbols.DOT:
			return ".";
		case BaseSymbols.COLON:
			return ":";
		case BaseSymbols.SEMICOLON:
			return ";";
		case BaseSymbols.COMMA:
			return ",";
		case BaseSymbols.EQUALS:
			return "=";
		case BaseSymbols.OPEN_PARENTHESIS:
			return "(";
		case BaseSymbols.CLOSE_PARENTHESIS:
			return ")";
		case BaseSymbols.OPEN_SQUARE_BRACKET:
			return "[";
		case BaseSymbols.CLOSE_SQUARE_BRACKET:
			return "]";
		case BaseSymbols.OPEN_CURLY_BRACKET:
			return "{";
		case BaseSymbols.CLOSE_CURLY_BRACKET:
			return "}";
		}
		return null;
	}
	
	@Override
	public int getLength(int type)
	{
		return 1;
	}
	
	private static void addReplacement(char symbol, String replacement)
	{
		Character c = symbol;
		symbolMap.put(c, replacement);
		replacementMap.put(replacement, c);
	}
	
	private static boolean isSymbol(char c)
	{
		return c == '=' || c == '>' || c == '<' || c == '+' || c == '-' || c == '*' || c == '/' || c == '!' || c == '@' || c == '#' || c == '%' || c == '^'
				|| c == '&' || c == '~' || c == '?' || c == '|' || c == '\\' || c == ':' || c == '.';
	}
	
	public static String qualify(String s)
	{
		int len = s.length();
		StringBuilder builder = new StringBuilder(len);
		for (int i = 0; i < len; i++)
		{
			char c = s.charAt(i);
			if (isSymbol(c))
			{
				String replacement = symbolMap.get(c);
				builder.append('$');
				builder.append(replacement);
			}
			else
			{
				builder.append(c);
			}
		}
		
		return builder.toString();
	}
	
	public static String unqualify(String s)
	{
		int i = s.indexOf('$');
		if (i == -1)
		{
			return s;
		}
		
		int len = s.length();
		StringBuilder builder = new StringBuilder(len);
		
		for (int j = 0; j < i; j++)
		{
			builder.append(s.charAt(j));
		}
		
		for (; i < len; i++)
		{
			char c = s.charAt(i);
			if (c == '$')
			{
				int index = indexOfNonLetter(s, i + 1, len);
				String s1 = s.substring(i + 1, index);
				if (s1.isEmpty())
				{
					builder.append('$');
					continue;
				}
				Character replacement = replacementMap.get(s1);
				if (replacement != null)
				{
					builder.append(replacement.charValue());
					i = index - 1;
					continue;
				}
			}
			
			builder.append(c);
		}
		return builder.toString();
	}
	
	private static int indexOfNonLetter(String s, int start, int end)
	{
		for (; start < end; start++)
		{
			char c = s.charAt(start);
			if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')
			{
				continue;
			}
			return start;
		}
		return end;
	}
}
