package dyvil.collection;

import dyvil.annotation.infix;
import dyvil.collection.immutable.ImmutableMap;
import dyvil.collection.mutable.MutableMap;

public interface JavaMaps
{
	public static @infix <K, V> MutableMap<K, V> mutable(java.util.Map<K, V> map)
	{
		MutableMap<K, V> newMap = new dyvil.collection.mutable.HashMap();
		for (java.util.Map.Entry<K, V> entry : map.entrySet())
		{
			newMap.update(entry.getKey(), entry.getValue());
		}
		return newMap;
	}
	
	public static @infix <K, V> ImmutableMap<K, V> immutable(java.util.Map<K, V> map)
	{
		return null; // FIXME
	}
}
