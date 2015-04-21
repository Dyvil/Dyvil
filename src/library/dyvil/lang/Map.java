package dyvil.lang;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import dyvil.collection.immutable.ImmutableMap;
import dyvil.collection.mutable.MutableMap;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;
import dyvil.lang.tuple.Tuple2;

public interface Map<K, V> extends Iterable<Tuple2<K, V>>, ArrayConvertible, NilConvertible
{	
	public static <K, V> MutableMap<K, V> apply()
	{
		return MutableMap.apply();
	}
	
	public static <K, V> ImmutableMap<K, V> apply(K key, V value)
	{
		return ImmutableMap.apply(key, value);
	}
	
	public static <K, V> ImmutableMap<K, V> apply(Tuple2<K, V> entry)
	{
		return ImmutableMap.apply(entry._1, entry._2);
	}
	
	public static <K, V> ImmutableMap<K, V> apply(Tuple2<? extends K, ? extends V>[] entries)
	{
		return ImmutableMap.apply(entries);
	}
	
	// Simple Getters
	
	/**
	 * Returns the size of this map, i.e. the number of mappings contained in
	 * this map.
	 */
	public int size();
	
	/**
	 * Returns true if and if only this map contains no mappings. The standard
	 * implementation defines a map as empty if it's size as calculated by
	 * {@link #size()} is exactly {@code 0}.
	 * 
	 * @return true, if this map contains no mappings
	 */
	public boolean isEmpty();
	
	/**
	 * Creates and returns an {@link Iterator} over the mappings of this map,
	 * packed in {@linkplain Tuple2 Tuples} containing the key as their first
	 * value and the value as their second value.
	 * 
	 * @return an iterator over the mappings of this map
	 */
	@Override
	public Iterator<Tuple2<K, V>> iterator();
	
	/**
	 * Creates and returns an {@link Spliterator} over the mappings of this map,
	 * packed in {@linkplain Tuple2 Tuples} containing the key as their first
	 * value and the value as their second value.
	 * 
	 * @return an iterator over the mappings of this map
	 */
	@Override
	public default Spliterator<Tuple2<K, V>> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), 0);
	}
	
	public Iterator<K> keyIterator();
	
	public Iterator<V> valueIterator();
	
	@Override
	public void forEach(Consumer<? super Tuple2<K, V>> action);
	
	public void forEach(BiConsumer<? super K, ? super V> action);
	
	/**
	 * Returns true if and if only this map contains a mapping for the given
	 * {@code key}.
	 * 
	 * @param key
	 *            the key
	 * @return true, if this map contains a mapping for the key
	 */
	public boolean $qmark(Object key);
	
	/**
	 * Returns true if and if only this map contains a mapping that maps the
	 * given {@code key} to the given {@code value}.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return true, if this map contains a mapping for the key and the value
	 */
	public boolean $qmark(Object key, Object value);
	
	/**
	 * Returns true if and if only this map contains a mapping that maps the
	 * key, as given by the first value of the {@code entry} to the value, as
	 * given by the second value of the {@code entry}. The default
	 * implementation of this method delegates to the {@link $qmark(Object,
	 * Object)} method.
	 * 
	 * @param entry
	 *            the entry
	 * @return true, if this map contains the mapping represented by the entry
	 */
	public default boolean $qmark(Tuple2<? extends K, ? extends V> entry)
	{
		return this.$qmark(entry._1, entry._2);
	}
	
	/**
	 * Returns true if and if only this map contains a mapping to the given
	 * {@code value}.
	 * 
	 * @param value
	 *            the value
	 * @return true, if this map contains a mapping to the value
	 */
	public boolean $qmark$colon(V value);
	
	/**
	 * Gets and returns the value for the given {@code key}. If no mapping for
	 * the {@code key} exists, {@code null} is returned.
	 * 
	 * @param key
	 *            the key
	 * @return the value
	 */
	public V apply(K key);
	
	// Non-mutating Operations
	
	/**
	 * Returns a map that contains all mappings of this map plus the new mapping
	 * specified by {@code key} and {@code value}. It depends on the type of
	 * this map if this method returns a new map or the mapping is simply added
	 * to this map.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return a map that contains all mappings of this map plus the new mapping
	 */
	public Map<K, V> $plus(K key, V value);
	
	public default Map<K, V> $plus(Tuple2<? extends K, ? extends V> entry)
	{
		return this.$plus(entry._1, entry._2);
	}
	
	public Map<K, V> $plus(Map<? extends K, ? extends V> map);
	
	public Map<K, V> $minus(K key);
	
	public Map<K, V> $minus(K key, V value);
	
	public default Map<K, V> $minus(Tuple2<? extends K, ? extends V> entry)
	{
		return this.$minus(entry._1, entry._2);
	}
	
	public Map<K, V> $minus$colon(V value);
	
	public Map<K, V> $minus(Map<? extends K, ? extends V> map);
	
	public <U> Map<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper);
	
	public Map<K, V> filtered(BiPredicate<? super K, ? super V> condition);
	
	// Mutating Operations
	
	public void clear();
	
	public void update(K key, V value);
	
	public default void $plus$eq(K key, V value)
	{
		this.update(key, value);
	}
	
	public default void $plus$eq(Tuple2<? extends K, ? extends V> entry)
	{
		this.update(entry._1, entry._2);
	}
	
	public void $plus$eq(Map<? extends K, ? extends V> map);
	
	public void $minus$eq(K key);
	
	public void $minus$eq(K key, V value);
	
	public default void $minus$eq(Tuple2<? extends K, ? extends V> entry)
	{
		this.$minus$eq(entry._1, entry._2);
	}
	
	public void $minus$colon$eq(V value);
	
	public void $minus$eq(Map<? extends K, ? extends V> map);
	
	public void map(BiFunction<? super K, ? super V, ? extends V> mapper);
	
	public void filter(BiPredicate<? super K, ? super V> condition);
	
	// Copying
	
	public Map<K, V> copy();
	
	public MutableMap<K, V> mutable();
	
	public ImmutableMap<K, V> immutable();
}
