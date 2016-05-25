package dyvil.tools.repl.lang;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18n
{
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("dyvil.tools.repl.lang.REPL");

	public static String get(String key)
	{
		try
		{
			return BUNDLE.getString(key);
		}
		catch (MissingResourceException ex)
		{
			return '#' + key;
		}
	}

	public static String get(String key, Object... args)
	{
		return String.format(get(key), args);
	}
}
