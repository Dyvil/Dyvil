package dyvil.tools.gensrc.lang;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class I18n
{
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("dyvil.tools.gensrc.lang.GenSrc");

	private I18n()
	{
	}

	public static String get(String key)
	{
		try
		{
			return BUNDLE.getString(key);
		}
		catch (MissingResourceException ignored)
		{
			return '#' + key;
		}
	}

	public static String get(String key, Object... args)
	{
		return String.format(get(key), args);
	}
}
