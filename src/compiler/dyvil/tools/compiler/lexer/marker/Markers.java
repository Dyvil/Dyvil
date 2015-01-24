package dyvil.tools.compiler.lexer.marker;

import java.util.HashMap;
import java.util.Map;

import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.I18n;

@FunctionalInterface
public interface Markers
{
	public static Markers				INFO	= (ICodePosition position, String text) -> new Info(position, text);
	public static Markers				WARNING	= (ICodePosition position, String text) -> new Warning(position, text);
	public static Markers				ERROR	= (ICodePosition position, String text) -> new SemanticError(position, text);
	
	public static Map<String, Markers>	map		= new HashMap();
	
	public static Markers get(String key)
	{
		Markers m = map.get(key);
		if (m == null)
		{
			switch (I18n.getString("marker." + key))
			{
			case "info":
				m = INFO;
				break;
			case "warning":
				m = WARNING;
				break;
			default:
				m = ERROR;
			}
			
			map.put(key, m);
		}
		return m;
	}
	
	public static Marker create(ICodePosition position, String key)
	{
		return get(key).createMarker(position, I18n.getString(key));
	}
	
	public static Marker create(ICodePosition position, String key, Object... args)
	{
		return get(key).createMarker(position, I18n.getString(key, args));
	}
	
	public abstract Marker createMarker(ICodePosition position, String text);
}
