package dyvil.tools.compiler.config;

import dyvil.collection.Map;
import dyvil.collection.mutable.HashMap;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Formatting
{
	private static ResourceBundle BUNDLE = ResourceBundle.getBundle("dyvil.tools.compiler.config.Formatting");

	private static Map<String, Boolean> booleanMap = new HashMap<>();
	private static Map<String, Integer> integerMap = new HashMap<>();

	private Formatting()
	{
		// no instances
	}

	public static boolean getBoolean(String key)
	{
		Boolean bool = booleanMap.get(key);
		if (bool != null)
		{
			return bool;
		}

		try
		{
			bool = Boolean.valueOf(BUNDLE.getString(key));
			booleanMap.put(key, bool);
			return bool;
		}
		catch (MissingResourceException ex)
		{
			booleanMap.put(key, null);
			return false;
		}
	}

	public static int getInt(String key)
	{
		Integer integer = integerMap.get(key);
		if (integer != null)
		{
			return integer;
		}

		try
		{
			integer = Integer.valueOf(BUNDLE.getString(key));
			integerMap.put(key, integer);
			return integer;
		}
		catch (NumberFormatException | MissingResourceException ex)
		{
			integerMap.put(key, null);
			return 0;
		}
	}

	public static void appendSeparator(StringBuilder stringBuilder, String key, String separator)
	{
		if (getBoolean(key + ".space_before"))
		{
			stringBuilder.append(' ');
		}
		stringBuilder.append(separator);
		if (getBoolean(key + ".space_after"))
		{
			stringBuilder.append(' ');
		}
	}

	public static void appendSeparator(StringBuilder stringBuilder, String key, char separator)
	{
		if (getBoolean(key + ".space_before"))
		{
			stringBuilder.append(' ');
		}
		stringBuilder.append(separator);
		if (getBoolean(key + ".space_after"))
		{
			stringBuilder.append(' ');
		}
	}

	public static String getSeparator(String key, char character)
	{
		StringBuilder stringBuilder = new StringBuilder(3);
		appendSeparator(stringBuilder, key, character);
		return stringBuilder.toString();
	}
	
	public static String getIndent(String key, String prefix)
	{
		int level = getInt(key);
		StringBuilder builder = new StringBuilder(key.length() + level);
		builder.append(prefix);
		for (int i = 0; i < level; i++)
		{
			builder.append('\t');
		}
		return builder.toString();
	}
}
