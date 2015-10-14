package dyvil.tools.compiler.transform;

import java.util.HashMap;
import java.util.Map;

import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Symbols;
import dyvil.tools.parsing.lexer.Tokens;

// TODO Re-add #[ or something similar
public final class DyvilSymbols implements Symbols
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
	
	public static final int	HASH			= Tokens.SYMBOL | 0x00060000;
	public static final int	WILDCARD		= Tokens.SYMBOL | 0x00070000;
	public static final int	ARROW_OPERATOR	= Tokens.SYMBOL | 0x00080000;
	public static final int	ELLIPSIS		= Tokens.SYMBOL | 0x00090000;
	public static final int	GENERIC_CALL	= Tokens.SYMBOL | 0x000A0000;
	public static final int	AT				= Tokens.SYMBOL | 0x000B0000;
	
	public static final DyvilSymbols INSTANCE = new DyvilSymbols();
	
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
	
	@Override
	public int getSymbolType(String s)
	{
		switch (s)
		{
		case "_":
			return DyvilSymbols.WILDCARD;
		case "#":
			return DyvilSymbols.HASH;
		case "=>":
		case "\u21D2": // Rightwards double arrow
			return DyvilSymbols.ARROW_OPERATOR;
		case "...":
		case "\u2026": // Horizontal ellipsis
			return DyvilSymbols.ELLIPSIS;
		case "@":
			return DyvilSymbols.AT;
		}
		return BaseSymbols.INSTANCE.getSymbolType(s);
	}
	
	@Override
	public int getKeywordType(String value)
	{
		return DyvilKeywords.getKeywordType(value);
	}
	
	@Override
	public String toString(int type)
	{
		switch (type)
		{
		case DyvilSymbols.WILDCARD:
			return "_";
		case DyvilSymbols.HASH:
			return "#";
		case DyvilSymbols.AT:
			return "@";
		case DyvilSymbols.ARROW_OPERATOR:
			return "=>";
		case DyvilSymbols.ELLIPSIS:
			return "...";
		}
		return BaseSymbols.INSTANCE.toString(type);
	}
	
	@Override
	public int getLength(int type)
	{
		switch (type)
		{
		case DyvilSymbols.WILDCARD:
			return 1;
		case DyvilSymbols.HASH:
			return 1;
		case DyvilSymbols.AT:
			return 1;
		case DyvilSymbols.ARROW_OPERATOR:
			return 2;
		case DyvilSymbols.ELLIPSIS:
			return 3;
		}
		String s = DyvilKeywords.keywordToString(type);
		if (s == null)
		{
			return 1;
		}
		return s.length();
	}
}
