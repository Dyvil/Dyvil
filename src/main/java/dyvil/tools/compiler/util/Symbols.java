package dyvil.tools.compiler.util;

import java.util.HashMap;
import java.util.Map;

public class Symbols
{
	public static Map<Character, String>	symbolMap		= new HashMap();
	public static Map<String, Character>	replacementMap	= new HashMap();
	
	static
	{
		addReplacement('=', "eq");
		addReplacement('>', "greater");
		addReplacement('<', "less");
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
	
	private static void addReplacement(char symbol, String replacement)
	{
		Character c = symbol;
		symbolMap.put(c, replacement);
		replacementMap.put(replacement, c);
	}
	
	private static boolean isSymbol(char c)
	{
		return c == '=' || c == '>' || c == '<' || c == '+' || c == '-' || c == '*' || c == '/' || c == '!' || c == '@' || c == '#' || c == '%' || c == '^' || c == '&' || c == '~' || c == '?' || c == '|' || c == '\\' || c == ':' || c == '.';
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
		int len = s.length();
		StringBuilder builder = new StringBuilder(len);
		for (int i = 0; i < len; i++)
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
