package dyvil.tools.compiler.util;

import dyvil.io.AppendablePrintStream;
import dyvil.tools.parsing.marker.*;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.util.MarkerLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class Markers
{
	private Markers()
	{
		// do not instantiate
	}
	
	private static final ResourceBundle SEMANTIC_BUNDLE = ResourceBundle
			.getBundle("dyvil.tools.compiler.lang.SemanticMarkers");

	private static final ResourceBundle SYNTAX_BUNDLE = ResourceBundle
			.getBundle("dyvil.tools.compiler.lang.SyntaxMarkers");

	private static final ResourceBundle MARKER_LEVEL_BUNDLE = ResourceBundle
			.getBundle("dyvil.tools.compiler.config.MarkerLevels");

	public static String getSemantic(String key)
	{
		try
		{
			return SEMANTIC_BUNDLE.getString(key);
		}
		catch (MissingResourceException ex)
		{
			return "!" + key + "!";
		}
	}
	
	public static String getSemantic(String key, Object... args)
	{
		return String.format(getSemantic(key), args);
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

	public static String getSyntax(String key, Object... args)
	{
		return String.format(getSyntax(key), args);
	}
	
	static final Map<String, MarkerLevel> markerLevelMap = new HashMap<>();
	
	private static MarkerLevel getMarkerLevel(String key)
	{
		MarkerLevel m = markerLevelMap.get(key);
		if (m != null)
		{
			return m;
		}

		if (!MARKER_LEVEL_BUNDLE.containsKey(key))
		{
			markerLevelMap.put(key, MarkerLevel.ERROR);
			return MarkerLevel.ERROR;
		}

		try
		{
			switch (MARKER_LEVEL_BUNDLE.getString(key))
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
			// Should never happen - we check for 'containsKey' above
			m = MarkerLevel.ERROR;
		}

		markerLevelMap.put(key, m);
		return m;
	}
	
	public static Marker withText(ICodePosition position, MarkerLevel level, String text)
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
	
	public static Marker semantic(ICodePosition position, String key)
	{
		return withText(position, getMarkerLevel(key), getSemantic(key));
	}
	
	public static Marker semantic(ICodePosition position, MarkerLevel level, String key)
	{
		return withText(position, level, getSemantic(key));
	}
	
	public static Marker semantic(ICodePosition position, String key, Object... args)
	{
		return withText(position, getMarkerLevel(key), getSemantic(key, args));
	}
	
	public static Marker semantic(ICodePosition position, MarkerLevel level, String key, Object... args)
	{
		return withText(position, level, getSemantic(key, args));
	}
	
	public static Marker semanticError(ICodePosition position, String key)
	{
		return new SemanticError(position, getSemantic(key));
	}
	
	public static Marker semanticError(ICodePosition position, String key, Object... args)
	{
		return new SemanticError(position, getSemantic(key, args));
	}

	public static Marker syntaxWarning(ICodePosition position, String key)
	{
		return new Warning(position, getSyntax(key));
	}

	public static Marker syntaxWarning(ICodePosition position, String key, Object... args)
	{
		return new Warning(position, getSyntax(key, args));
	}

	public static Marker syntaxError(ICodePosition position, String key)
	{
		return new SyntaxError(position, getSyntax(key));
	}

	public static Marker syntaxError(ICodePosition position, String key, Object... args)
	{
		return new SyntaxError(position, getSyntax(key, args));
	}

	public static Marker parserError(ICodePosition position, Throwable ex)
	{
		final Marker marker = Markers.syntaxError(position, "parser.error", position.toString(), ex.getLocalizedMessage());
		appendThrowable(marker, ex);
		return marker;
	}

	public static void appendThrowable(Marker marker, Throwable ex)
	{
		final StringBuilder builder = new StringBuilder();
		ex.printStackTrace(new AppendablePrintStream(builder));
		marker.addInfo(builder.toString());
	}
}
