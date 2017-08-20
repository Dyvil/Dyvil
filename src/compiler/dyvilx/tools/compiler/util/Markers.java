package dyvilx.tools.compiler.util;

import dyvil.annotation.internal.NonNull;
import dyvil.source.position.SourcePosition;
import dyvil.util.I18n;
import dyvil.util.MarkerLevel;
import dyvilx.tools.parsing.marker.*;
import dyvilx.tools.parsing.marker.InfoMarker;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@SuppressWarnings( { "SameParameterValue", "unused" })
public final class Markers
{

	private static final ResourceBundle SEMANTIC_BUNDLE = ResourceBundle
		                                                      .getBundle("dyvilx.tools.compiler.lang.SemanticMarkers");

	private static final ResourceBundle SYNTAX_BUNDLE = ResourceBundle
		                                                    .getBundle("dyvilx.tools.compiler.lang.SyntaxMarkers");

	private static final ResourceBundle MARKER_LEVEL_BUNDLE = ResourceBundle.getBundle(
		"dyvilx.tools.compiler.config.MarkerLevels");

	public static final I18n INSTANCE = key -> {
		if (SEMANTIC_BUNDLE.containsKey(key))
		{
			return SEMANTIC_BUNDLE.getString(key);
		}
		if (SYNTAX_BUNDLE.containsKey(key))
		{
			return SYNTAX_BUNDLE.getString(key);
		}
		return BaseMarkers.INSTANCE.getString(key);
	};

	private Markers()
	{
		// do not instantiate
	}

	// Basic Key Mappers

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

	// Message Constructors

	@NonNull
	public static String message(String key, String semantic)
	{
		return semantic + " [" + key + ']';
	}

	public static String semanticMessage(String key)
	{
		return message(key, getSemantic(key));
	}

	public static String semanticMessage(String key, Object... args)
	{
		return message(key, getSemantic(key, args));
	}

	public static String syntaxMessage(String key)
	{
		return message(key, getSyntax(key));
	}

	public static String syntaxMessage(String key, Object... args)
	{
		return message(key, getSyntax(key, args));
	}

	// Marker Constructors

	private static final Map<String, MarkerLevel> MARKER_LEVEL_MAP = new HashMap<>();

	private static MarkerLevel getMarkerLevel(String key)
	{
		MarkerLevel level = MARKER_LEVEL_MAP.get(key);
		if (level != null)
		{
			return level;
		}

		if (!MARKER_LEVEL_BUNDLE.containsKey(key))
		{
			MARKER_LEVEL_MAP.put(key, MarkerLevel.ERROR);
			return MarkerLevel.ERROR;
		}

		try
		{
			switch (MARKER_LEVEL_BUNDLE.getString(key))
			{
			case "info":
				level = MarkerLevel.INFO;
				break;
			case "warning":
				level = MarkerLevel.WARNING;
				break;
			case "ignore":
				level = MarkerLevel.IGNORE;
				break;
			default:
				level = MarkerLevel.ERROR;
				break;
			}
		}
		catch (MissingResourceException ex)
		{
			// Should never happen - we check for 'containsKey' above
			level = MarkerLevel.ERROR;
		}

		MARKER_LEVEL_MAP.put(key, level);
		return level;
	}

	public static Marker withText(SourcePosition position, MarkerLevel level, String text)
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

	public static Marker semantic(SourcePosition position, String key)
	{
		return withText(position, getMarkerLevel(key), semanticMessage(key));
	}

	public static Marker semantic(SourcePosition position, String key, Object... args)
	{
		return withText(position, getMarkerLevel(key), semanticMessage(key, args));
	}

	public static Marker semantic(SourcePosition position, MarkerLevel level, String key)
	{
		return withText(position, level, semanticMessage(key));
	}

	public static Marker semantic(SourcePosition position, MarkerLevel level, String key, Object... args)
	{
		return withText(position, level, semanticMessage(key, args));
	}

	public static Marker semanticError(SourcePosition position, String key)
	{
		return new SemanticError(position, semanticMessage(key));
	}

	public static Marker semanticError(SourcePosition position, String key, Object... args)
	{
		return new SemanticError(position, semanticMessage(key, args));
	}

	public static Marker semanticWarning(SourcePosition position, String key)
	{
		return new Warning(position, semanticMessage(key));
	}

	public static Marker semanticWarning(SourcePosition position, String key, Object... args)
	{
		return new Warning(position, semanticMessage(key, args));
	}

	public static Marker syntaxWarning(SourcePosition position, String key)
	{
		return new Warning(position, syntaxMessage(key));
	}

	public static Marker syntaxWarning(SourcePosition position, String key, Object... args)
	{
		return new Warning(position, syntaxMessage(key, args));
	}

	public static Marker syntaxError(SourcePosition position, String key)
	{
		return new SyntaxError(position, syntaxMessage(key));
	}

	public static Marker syntaxError(SourcePosition position, String key, Object... args)
	{
		return new SyntaxError(position, syntaxMessage(key, args));
	}
}
