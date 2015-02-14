package dyvil.maps;

import java.util.Map;

import dyvil.lang.annotation.infix;
import dyvil.lang.annotation.inline;

public interface MapUtils
{
	public static @infix @inline <K, V> V $at(Map<K, V> map, K key)
	{
		return map.get(key);
	}
	
	public static @infix @inline <K, V> boolean $qmark(Map<K, V> map, K key)
	{
		return map.containsKey(key);
	}
	
	public static @infix @inline <K, V> void $plus$eq(Map<K, V> map, K key, V value)
	{
		map.put(key, value);
	}
	
	public static @infix @inline <K, V> void $minus$eq(Map<K, V> map, K key)
	{
		map.remove(key);
	}
}
