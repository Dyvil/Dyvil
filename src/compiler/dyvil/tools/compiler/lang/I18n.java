package dyvil.tools.compiler.lang;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18n
{
	public static final ResourceBundle MESSAGE_BUNDLE = ResourceBundle.getBundle("dyvil.tools.compiler.lang.Compiler");

	public static String get(String key)
	{
		try
		{
			return MESSAGE_BUNDLE.getString(key);
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
