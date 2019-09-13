package dyvilx.tools.parsing.marker;

import dyvil.util.I18n;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class BaseMarkers implements I18n
{
	public static final BaseMarkers INSTANCE = new BaseMarkers();

	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("dyvilx.tools.parsing.lang.SyntaxMarkers");

	private BaseMarkers()
	{
	}

	@Override
	public String getString(String key)
	{
		try
		{
			return BUNDLE.getString(key);
		}
		catch (MissingResourceException ex)
		{
			return '!' + key + '!';
		}
	}
}
