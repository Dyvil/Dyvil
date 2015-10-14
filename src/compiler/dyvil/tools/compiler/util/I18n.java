package dyvil.tools.compiler.util;

import java.util.*;

import dyvil.tools.parsing.marker.Info;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.SemanticError;
import dyvil.tools.parsing.marker.Warning;
import dyvil.tools.parsing.position.ICodePosition;

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
	
	public static enum MarkerType
	{
		IGNORE, INFO, WARNING, ERROR;
		
		static final Map<String, MarkerType> map = new HashMap();
	}
	
	public static MarkerType getMarkerType(String key)
	{
		MarkerType m = MarkerType.map.get(key);
		if (m == null)
		{
			switch (getString("marker." + key))
			{
			case "info":
				m = MarkerType.INFO;
				break;
			case "warning":
				m = MarkerType.WARNING;
				break;
			case "ignore":
				m = MarkerType.IGNORE;
				break;
			default:
				m = MarkerType.ERROR;
			}
			
			MarkerType.map.put(key, m);
		}
		return m;
	}
	
	public static Marker createMarker(ICodePosition position, String key)
	{
		MarkerType type = getMarkerType(key);
		switch (type)
		{
		case ERROR:
			return new SemanticError(position, I18n.getString(key));
		case INFO:
			return new Info(position, I18n.getString(key));
		case WARNING:
			return new Warning(position, I18n.getString(key));
		default:
			return null;
		}
	}
	
	public static Marker createMarker(ICodePosition position, String key, Object... args)
	{
		MarkerType type = getMarkerType(key);
		switch (type)
		{
		case ERROR:
			return new SemanticError(position, I18n.getString(key, args));
		case INFO:
			return new Info(position, I18n.getString(key, args));
		case WARNING:
			return new Warning(position, I18n.getString(key, args));
		default:
			return null;
		}
	}
}
