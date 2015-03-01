package dyvil.maps;

import java.util.Map;

import dyvil.lang.annotation.Utility;
import dyvil.lang.annotation.infix;
import dyvil.lang.annotation.inline;
import dyvil.lang.tuple.Tuple2;

/**
 * The {@linkplain Utility utility interface} <b>MapUtils</b> is used to extend
 * the {@link Map} interface with several syntactic sugar methods that can be
 * used from within <i>Dyvil</i> code. These include special operators for
 * adding or removing key-value-pairs or checking if the map contains a key.
 * 
 * @author Clashsoft
 * @version 1.0
 */
@Utility(Map.class)
public interface MapUtils
{
	/**
	 * Returns the value for the given {@code key} of the given {@link Map}
	 * {@code map} using the map's {@link Map#get(Object) get} method.
	 * 
	 * @param map
	 *            the map
	 * @param key
	 *            the key
	 * @return the value
	 */
	public static @infix @inline <K, V> V $at(Map<K, V> map, K key)
	{
		return map.get(key);
	}
	
	/**
	 * Returns true, if the given {@link Map} contains the given {@code key}
	 * using the map's {@link Map#containsKey(Object) containsKey} method.
	 * 
	 * @param map
	 *            the map
	 * @param key
	 *            the key
	 * @return true, if the map contains the key
	 */
	public static @infix @inline <K, V> boolean $qmark(Map<K, V> map, K key)
	{
		return map.containsKey(key);
	}
	
	/**
	 * Adds a mapping for the given {@code key} and the given {@code value} to
	 * the given {@link Map} {@code map} using the map's
	 * {@link Map#put(Object, Object) put} method.
	 * 
	 * @param map
	 *            the map
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public static @infix @inline <K, V> void $plus$eq(Map<K, V> map, K key, V value)
	{
		map.put(key, value);
	}
	
	/**
	 * Adds a mapping defined by the given {@linkplain Tuple2 Tuple}'s
	 * {@link Tuple2#_1 key} and {@link Tuple2#_2 value} to the given
	 * {@link Map} {@code map} using the map's {@link Map#put(Object, Object)
	 * put} method. This method is designed for <i>Dyvil</i>s special Tuple
	 * syntax {@code key -> value}. The {@code ->} operator has precedence over
	 * the {@code +=} operator, which means that one can add a mapping to a map
	 * with the following statement:
	 * 
	 * <PRE>
	 * Map map = ...
	 * map += "a" -> "b"
	 * </PRE>
	 * 
	 * @param map
	 *            the map
	 * @param tuple
	 *            the key-value-pair
	 */
	public static @infix @inline <K, V> void $plus$eq(Map<K, V> map, Tuple2<K, V> tuple)
	{
		map.put(tuple._1, tuple._2);
	}
	
	/**
	 * Removed the mapping for the given {@code key} from the given {@link Map}
	 * {@code map} using the map's {@link Map#remove(Object) remove} method.
	 * 
	 * @param map
	 *            the map
	 * @param key
	 *            the key to remove
	 */
	public static @infix @inline <K, V> void $minus$eq(Map<K, V> map, K key)
	{
		map.remove(key);
	}
}
