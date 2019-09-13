package dyvilx.tools.gensrc.lang;

import dyvilx.tools.compiler.util.Markers;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class I18n
{
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("dyvilx.tools.gensrc.lang.GenSrc");

	private I18n()
	{
	}

	public static final dyvil.util.I18n INSTANCE = I18n::get;

	public static final dyvil.util.I18n SYNTAX = I18n::getSyntax;

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

	public static String getSyntax(String key)
	{
		try
		{
			return BUNDLE.getString(key);
		}
		catch (MissingResourceException ignored)
		{
			return Markers.getSyntax(key);
		}
	}

	public static String get(String key, Object... args)
	{
		return String.format(get(key), args);
	}
}
