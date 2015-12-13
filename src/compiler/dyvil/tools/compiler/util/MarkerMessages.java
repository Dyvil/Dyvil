package dyvil.tools.compiler.util;

import dyvil.tools.parsing.marker.*;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.util.MarkerLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class MarkerMessages
{
	private MarkerMessages()
	{
		// do not instantiate
	}
	
	private static final ResourceBundle LOCALIZATION_BUNDLE = ResourceBundle
			.getBundle("dyvil.tools.compiler.lang.MarkerMessages");
	private static final ResourceBundle MARKER_BUNDLE       = ResourceBundle
			.getBundle("dyvil.tools.compiler.config.MarkerLevels");
	private static final ResourceBundle SYNTAX_BUNDLE       = ResourceBundle
			.getBundle("dyvil.tools.compiler.config.SyntaxErrors");

	public static String getMarker(String key)
	{
		try
		{
			return LOCALIZATION_BUNDLE.getString(key);
		}
		catch (MissingResourceException ex)
		{
			return "!" + key + "!";
		}
	}
	
	public static String getMarker(String key, Object... args)
	{
		return String.format(getMarker(key), args);
	}

	public static String getSyntax(String key)
	{
		try
		{
			return SYNTAX_BUNDLE.getString(key);
		}
		catch (MissingResourceException ex)
		{
			return "!" + key + "!";
		}
	}
	
	static final Map<String, MarkerLevel> markerLevelMap = new HashMap<>();
	
	private static MarkerLevel getMarkerLevel(String key)
	{
		MarkerLevel m = markerLevelMap.get(key);
		if (m == null)
		{
			try
			{
				switch (MARKER_BUNDLE.getString(key))
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
					break;
				}
			}
			catch (MissingResourceException ex)
			{
				m = MarkerLevel.ERROR;
			}
			
			markerLevelMap.put(key, m);
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
			return new InfoMarker(position, text);
		case WARNING:
			return new Warning(position, text);
		default:
			return null;
		}
	}
	
	public static Marker createMarker(ICodePosition position, String key)
	{
		return createTextMarker(position, getMarkerLevel(key), getMarker(key));
	}
	
	public static Marker createMarker(ICodePosition position, MarkerLevel level, String key)
	{
		return createTextMarker(position, level, getMarker(key));
	}
	
	public static Marker createMarker(ICodePosition position, String key, Object... args)
	{
		return createTextMarker(position, getMarkerLevel(key), getMarker(key, args));
	}
	
	public static Marker createMarker(ICodePosition position, MarkerLevel level, String key, Object... args)
	{
		return createTextMarker(position, level, getMarker(key, args));
	}
	
	public static Marker createError(ICodePosition position, String key)
	{
		return new SemanticError(position, getMarker(key));
	}
	
	public static Marker createError(ICodePosition position, String key, Object... args)
	{
		return new SemanticError(position, getMarker(key, args));
	}

	public static Marker createSyntaxError(ICodePosition position, String key)
	{
		return new SyntaxError(position, getSyntax(key));
	}
}
