package dyvil.tools.compiler.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18n
{
	private I18n()
	{
		// do not instantiate
	}
	
	private static final String			BUNDLE_NAME		= "dyvil.tools.compiler.lang.lang";	//$NON-NLS-1$
	private static final ResourceBundle	RESOURCE_BUNDLE	= loadBundle();
	
	private static ResourceBundle loadBundle()
	{
		Locale.setDefault(Locale.ENGLISH);
		return ResourceBundle.getBundle(BUNDLE_NAME, Locale.ENGLISH);
	}
	
	public static String getString(String key)
	{
		try
		{
			return RESOURCE_BUNDLE.getString(key);
		}
		catch (MissingResourceException e)
		{
			return "!" + key + "!";
		}
	}
	
	public static String getString(String key, Object... args)
	{
		return String.format(getString(key), args);
	}
}
