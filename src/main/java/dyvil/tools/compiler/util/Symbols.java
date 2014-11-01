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
	}
	
	private static void addReplacement(char symbol, String replacement)
	{
		Character c = symbol;
		symbolMap.put(c, replacement);
		replacementMap.put(replacement, c);
	}
	
	private static boolean isSymbol(char c)
	{
		return c == '=' || c == '>' || c == '<' || c == '+' || c == '-' || c == '*' || c == '/' || c == '!' || c == '@' || c == '#' || c == '%' || c == '^' || c == '&' || c == '~' || c == '?' || c == '|' || c == '\\' || c == ':';
	}
	
	public static String expand(String s)
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
}
