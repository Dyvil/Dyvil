package dyvil.tools.compiler.util;

import java.util.*;

import dyvil.tools.parsing.marker.Info;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.SemanticError;
import dyvil.tools.parsing.marker.Warning;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.util.MarkerLevel;

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
	
	static final Map<String, MarkerLevel> map = new HashMap();
	
	private static MarkerLevel getMarkerLevel(String key)
	{
		MarkerLevel m = map.get(key);
		if (m == null)
		{
			switch (getString("marker." + key))
			{
			case "info":
				m = MarkerLevel.INFO;
				break;
			case "warning":
				m = MarkerLevel.WARNING;
				break;
			case "ignore":
				m = MarkerLevel.IGNORE;
				break;
			default:
				m = MarkerLevel.ERROR;
			}
			
			map.put(key, m);
		}
		return m;
	}
	
	public static Marker createTextMarker(ICodePosition position, MarkerLevel level, String text)
	{
		switch (level)
		{
		case ERROR:
			return new SemanticError(position, text);
		case INFO:
			return new Info(position, text);
		case WARNING:
			return new Warning(position, text);
		default:
			return null;
		}
	}
	
	public static Marker createMarker(ICodePosition position, String key)
	{
		return createTextMarker(position, getMarkerLevel(key), getString(key));
	}
	
	public static Marker createMarker(ICodePosition position, MarkerLevel level, String key)
	{
		return createTextMarker(position, level, getString(key));
	}
	
	public static Marker createMarker(ICodePosition position, String key, Object... args)
	{
		return createTextMarker(position, getMarkerLevel(key), getString(key, args));
	}
	
	public static Marker createMarker(ICodePosition position, MarkerLevel level, String key, Object... args)
	{
		return createTextMarker(position, level, getString(key, args));
	}
	
	public static Marker createError(ICodePosition position, String key)
	{
		return new SemanticError(position, getString(key));
	}
	
	public static Marker createError(ICodePosition position, String key, Object... args)
	{
		return new SemanticError(position, getString(key, args));
	}
}
