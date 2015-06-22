package dyvil.collection;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import dyvil.lang.Map;

import dyvil.annotation.infix;
import dyvil.annotation.inline;
import dyvil.collection.immutable.ArrayMap;
import dyvil.collection.immutable.EmptyMap;
import dyvil.collection.immutable.SingletonMap;
import dyvil.tuple.Tuple2;

public interface JavaMaps
{
	// Accessors
	
	/**
	 * @see Map#containsKey(Object)
	 */
	public static @infix @inline boolean $qmark(java.util.Map map, Object key)
	{
		return map.containsKey(key);
	}
	
	/**
	 * @see Map#contains(Object, Object)
	 */
	public static @infix boolean $qmark(java.util.Map map, Object key, Object value)
	{
		return value == null ? map.get(key) == null : value.equals(map.get(key));
	}
	
	/**
	 * @see Map#contains(Tuple2)
	 */
	public static @infix boolean $qmark(java.util.Map map, Tuple2 entry)
	{
		return $qmark(map, entry._1, entry._2);
	}
	
	/**
	 * @see Map#containsValue(Object)
	 */
	public static @infix @inline boolean $qmark$colon(java.util.Map map, Object value)
	{
		return map.containsValue(value);
	}
	
	/**
	 * @see Map#get(Object)
	 */
	public static @infix @inline <K, V> V apply(java.util.Map<K, V> map, K key)
	{
		return map.get(key);
	}
	
	// Mutating Operations
	
	/**
	 * @see Map#subscript_$eq(Object, Object)
	 */
	public static @infix @inline <K, V> void update(java.util.Map<K, V> map, K key, V value)
	{
		map.put(key, value);
	}
	
	/**
	 * @see Map#$plus$eq(Tuple2)
	 */
	public static @infix @inline <K, V> void $plus$eq(java.util.Map<K, V> map, Tuple2<? extends K, ? extends V> entry)
	{
		map.put(entry._1, entry._2);
	}
	
	/**
	 * @see Map#$plus$plus$eq(Map)
	 */
	public static @infix @inline <K, V> void $plus$plus$eq(java.util.Map<K, V> map1, java.util.Map<K, V> map2)
	{
		map1.putAll(map2);
	}
	
	/**
	 * @see Map#$minus$eq(Tuple2)
	 */
	public static @infix @inline <K, V> void $minus$eq(java.util.Map<K, V> map, Tuple2<? super K, ? super V> entry)
	{
		map.remove(entry._1, entry._2);
	}
	
	/**
	 * @see Map#$minus$colon$eq(Object)
	 */
	public static @infix <K, V> void $minus$colon$eq(java.util.Map<K, V> map, Object value)
	{
		map.values().remove(value);
	}
	
	/**
	 * @see Map#$minus$minus$eq(Map)
	 */
	public static @infix <K, V> void $minus$minus$eq(java.util.Map<K, V> map1, java.util.Map<? super K, ? super V> map2)
	{
		for (java.util.Map.Entry<? super K, ? super V> e : map2.entrySet())
		{
			map1.remove(e.getKey(), e.getValue());
		}
	}
	
	/**
	 * @see Map#map(BiFunction)
	 */
	public static @infix @inline <K, V> void map(java.util.Map<K, V> map, BiFunction<? super K, ? super V, ? extends V> mapper)
	{
		map.replaceAll(mapper);
	}
	
	/**
	 * @see Map#filter(BiPredicate)
	 */
	public static @infix <K, V> void filter(java.util.Map<K, V> map, BiPredicate<? super K, ? super V> condition)
	{
		Iterator<java.util.Map.Entry<K, V>> iterator = map.entrySet().iterator();
		while (iterator.hasNext())
		{
			java.util.Map.Entry<K, V> entry = iterator.next();
			if (!condition.test(entry.getKey(), entry.getValue()))
			{
				iterator.remove();
			}
		}
	}
	
	/**
	 * @see Map#mutable()
	 */
	public static @infix <K, V> MutableMap<K, V> mutable(java.util.Map<K, V> map)
	{
		MutableMap<K, V> newMap = new dyvil.collection.mutable.HashMap();
		for (java.util.Map.Entry<K, V> entry : map.entrySet())
		{
			newMap.subscript_$eq(entry.getKey(), entry.getValue());
		}
		return newMap;
	}
	
	/**
	 * @see Map#immutable()
	 */
	public static @infix <K, V> ImmutableMap<K, V> immutable(java.util.Map<K, V> map)
	{
		int len = map.size();
		switch (len)
		{
		case 0:
			return EmptyMap.apply();
		case 1:
			java.util.Map.Entry<K, V> entry = map.entrySet().iterator().next();
			return SingletonMap.apply(entry.getKey(), entry.getValue());
		default:
		}
		
		Object[] keys = new Object[len];
		Object[] values = new Object[len];
		
		int index = 0;
		for (java.util.Map.Entry<K, V> entry : map.entrySet())
		{
			keys[index] = entry.getKey();
			values[index] = entry.getValue();
			index++;
		}
		return new ArrayMap(keys, values, len, true);
	}
}
