package dyvil.collection;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import dyvil.tuple.Tuple2;

@NilConvertible
@ArrayConvertible
public interface Map<K, V> extends Iterable<Entry<K, V>>
{
	public static <K, V> MutableMap<K, V> apply()
	{
		return MutableMap.apply();
	}
	
	public static <K, V> ImmutableMap<K, V> apply(Entry<K, V> entry)
	{
		return ImmutableMap.apply(entry);
	}
	
	public static <K, V> ImmutableMap<K, V> apply(Tuple2<? extends K, ? extends V>... entries)
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
	public default boolean isEmpty()
	{
		return this.size() == 0;
	}
	
	/**
	 * Creates and returns an {@link Iterator} over the mappings of this map,
	 * packed in {@linkplain Entry Tuples} containing the key as their first
	 * value and the value as their second value.
	 * 
	 * @return an iterator over the mappings of this map
	 */
	@Override
	public Iterator<Entry<K, V>> iterator();
	
	/**
	 * Creates and returns an {@link Spliterator} over the mappings of this map,
	 * packed in {@linkplain Entry Tuples} containing the key as their first
	 * value and the value as their second value.
	 * 
	 * @return an iterator over the mappings of this map
	 */
	@Override
	public default Spliterator<Entry<K, V>> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), 0);
	}
	
	public default Stream<Entry<K, V>> stream()
	{
		return StreamSupport.stream(this.spliterator(), false);
	}
	
	public default Stream<Entry<K, V>> parallelStream()
	{
		return StreamSupport.stream(this.spliterator(), true);
	}
	
	public Iterator<K> keyIterator();
	
	public default Spliterator<K> keySpliterator()
	{
		return Spliterators.spliterator(this.keyIterator(), this.size(), 0);
	}
	
	public default Stream<K> keyStream()
	{
		return StreamSupport.stream(this.keySpliterator(), false);
	}
	
	public default Stream<K> parallelKeyStream()
	{
		return StreamSupport.stream(this.keySpliterator(), true);
	}
	
	public Iterator<V> valueIterator();
	
	public default Spliterator<V> valueSpliterator()
	{
		return Spliterators.spliterator(this.valueIterator(), this.size(), 0);
	}
	
	public default Stream<V> valueStream()
	{
		return StreamSupport.stream(this.valueSpliterator(), false);
	}
	
	public default Stream<V> parallelValueStream()
	{
		return StreamSupport.stream(this.valueSpliterator(), true);
	}
	
	@Override
	public default void forEach(Consumer<? super Entry<K, V>> action)
	{
		for (Entry<K, V> entry : this)
		{
			action.accept(entry);
		}
	}
	
	public default void forEach(BiConsumer<? super K, ? super V> action)
	{
		for (Entry<K, V> entry : this)
		{
			action.accept(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * Returns true if and if only this map contains a mapping for the given
	 * {@code key}.
	 * 
	 * @param key
	 *            the key
	 * @return true, if this map contains a mapping for the key
	 */
	public default boolean $qmark(Object key)
	{
		return this.containsKey(key);
	}
	
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
	public default boolean $qmark(Object key, Object value)
	{
		return this.contains(key, value);
	}
	
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
	public default boolean $qmark(Entry<?, ?> entry)
	{
		return this.contains(entry.getKey(), entry.getValue());
	}
	
	/**
	 * Returns true if and if only this map contains a mapping to the given
	 * {@code value}.
	 * 
	 * @param value
	 *            the value
	 * @return true, if this map contains a mapping to the value
	 */
	public default boolean $qmark$colon(Object value)
	{
		return this.containsValue(value);
	}
	
	/**
	 * Returns true if and if only this map contains a mapping for the given
	 * {@code key}.
	 * 
	 * @param key
	 *            the key
	 * @return true, if this map contains a mapping for the key
	 */
	public boolean containsKey(Object key);
	
	/**
	 * Returns true if and if only this map contains a mapping to the given
	 * {@code value}.
	 * 
	 * @param value
	 *            the value
	 * @return true, if this map contains a mapping to the value
	 */
	public boolean containsValue(Object value);
	
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
	public boolean contains(Object key, Object value);
	
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
	public default boolean contains(Entry<?, ?> entry)
	{
		return this.contains(entry.getKey(), entry.getValue());
	}
	
	/**
	 * Gets and returns the value for the given {@code key}. If no mapping for
	 * the {@code key} exists, {@code null} is returned. This alias forwarder
	 * for Dyvil Subscript Syntax delegates to {@link #get(Object)}.
	 * 
	 * @param key
	 *            the key
	 * @return the value
	 */
	public default V subscript(Object key)
	{
		return this.get(key);
	}
	
	/**
	 * Gets and returns the value for the given {@code key}. If no mapping for
	 * the {@code key} exists, {@code null} is returned.
	 * 
	 * @param key
	 *            the key
	 * @return the value
	 */
	public V get(Object key);
	
	// Non-mutating Operations
	
	/**
	 * Returns a map that contains all entries of this map plus the new entry
	 * specified by {@code key} and {@code value} as if it were added by
	 * {@link #subscript_$eq(Object, Object)}. If the {@code key} is already
	 * present in this map, a map is returned that uses the given {@code value}
	 * instead of the previous value for the {@code key}, and that has the same
	 * size as this map.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return a map that contains all entries of this map plus the new entry
	 */
	public Map<K, V> $plus(K key, V value);
	
	/**
	 * Returns a map that contains all entries of this map plus the new
	 * {@code entry}, as if it were added by
	 * {@link #subscript_$eq(Object, Object)}. If the {@code key} is already
	 * present in this map, a map is returned that uses the given {@code value}
	 * instead of the previous value for the {@code key}, and that has the same
	 * size as this map.
	 * 
	 * @see #$plus(Object, Object)
	 * @param entry
	 *            the entry
	 * @return a map that contains all entries of this map plus the new entry
	 */
	public default Map<K, V> $plus(Entry<? extends K, ? extends V> entry)
	{
		return this.$plus(entry.getKey(), entry.getValue());
	}
	
	/**
	 * Returns a map that contains all entries of this map plus all entries of
	 * the given {@code map}, as if they were added by
	 * {@link #subscript_$eq(Object, Object)}. If a key in the given map is
	 * already present in this map, a map is returned that uses the value from
	 * the given {@code map} for that key.
	 * 
	 * @param map
	 * @return
	 */
	public Map<K, V> $plus$plus(Map<? extends K, ? extends V> map);
	
	public Map<K, V> $minus(Object key);
	
	public Map<K, V> $minus(Object key, Object value);
	
	public default Map<K, V> $minus(Entry<?, ?> entry)
	{
		return this.$minus(entry.getKey(), entry.getValue());
	}
	
	public Map<K, V> $minus$colon(Object value);
	
	public Map<K, V> $minus$minus(Map<? super K, ? super V> map);
	
	public <U> Map<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper);
	
	public <U, R> Map<U, R> entryMapped(BiFunction<? super K, ? super V, ? extends Entry<? extends U, ? extends R>> mapper);
	
	public <U, R> Map<U, R> flatMapped(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends U, ? extends R>>> mapper);
	
	public Map<K, V> filtered(BiPredicate<? super K, ? super V> condition);
	
	// Mutating Operations
	
	public default void $plus$eq(K key, V value)
	{
		this.subscript_$eq(key, value);
	}
	
	public default void $plus$eq(Entry<? extends K, ? extends V> entry)
	{
		this.subscript_$eq(entry.getKey(), entry.getValue());
	}
	
	public default void $plus$plus$eq(Map<? extends K, ? extends V> map)
	{
		for (Entry<? extends K, ? extends V> entry : map)
		{
			this.subscript_$eq(entry.getKey(), entry.getValue());
		}
	}
	
	public default void $minus$eq(Object key)
	{
		this.removeKey(key);
	}
	
	public default void $minus$eq(Object key, Object value)
	{
		this.remove(key, value);
	}
	
	public default void $minus$eq(Entry<?, ?> entry)
	{
		this.remove(entry.getKey(), entry.getValue());
	}
	
	public default void $minus$colon$eq(Object value)
	{
		this.removeValue(value);
	}
	
	public default void $minus$minus$eq(Collection<?> keys)
	{
		for (Object key : keys)
		{
			this.$minus$eq(key);
		}
	}
	
	public default void $minus$minus$eq(Map<?, ?> map)
	{
		for (Entry<?, ?> entry : map)
		{
			this.$minus$eq(entry);
		}
	}
	
	public void clear();
	
	public void subscript_$eq(K key, V value);
	
	public V put(K key, V value);
	
	public V put(Entry<? extends K, ? extends V> entry);
	
	public boolean putAll(Map<? extends K, ? extends V> map);
	
	public V removeKey(Object key);
	
	public boolean removeValue(Object value);
	
	public boolean remove(Object key, Object value);
	
	public boolean remove(Entry<?, ?> entry);
	
	public boolean removeKeys(Collection<?> keys);
	
	public boolean removeAll(Map<?, ?> map);
	
	public void map(BiFunction<? super K, ? super V, ? extends V> mapper);
	
	public void mapEntries(BiFunction<? super K, ? super V, ? extends Entry<? extends K, ? extends V>> mapper);
	
	public void flatMap(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends K, ? extends V>>> mapper);
	
	public void filter(BiPredicate<? super K, ? super V> condition);
	
	// Copying
	
	public Map<K, V> copy();
	
	public MutableMap<K, V> mutable();
	
	public MutableMap<K, V> mutableCopy();
	
	public ImmutableMap<K, V> immutable();
	
	public ImmutableMap<K, V> immutableCopy();
	
	// toString, equals and hashCode
	
	@Override
	public String toString();
	
	@Override
	public boolean equals(Object obj);
	
	@Override
	public int hashCode();
	
	public static <K, V> String mapToString(Map<K, V> map)
	{
		if (map.isEmpty())
		{
			return "[]";
		}
		
		StringBuilder builder = new StringBuilder("[ ");
		Iterator<Entry<K, V>> iterator = map.iterator();
		while (true)
		{
			Entry<K, V> entry = iterator.next();
			builder.append(entry.getKey()).append(" -> ").append(entry.getValue());
			if (iterator.hasNext())
			{
				builder.append(", ");
			}
			else
			{
				break;
			}
		}
		return builder.append(" ]").toString();
	}
	
	public static <K, V> boolean mapEquals(Map<K, V> map, Object obj)
	{
		if (!(obj instanceof Map))
		{
			return false;
		}
		
		return mapEquals(map, (Map) obj);
	}
	
	public static <K, V> boolean mapEquals(Map<K, V> map1, Map<K, V> map2)
	{
		if (map1.size() != map2.size())
		{
			return false;
		}
		
		for (Entry<K, V> e : map1)
		{
			if (!map2.contains(e))
			{
				return false;
			}
		}
		return true;
	}
	
	public static <K, V> int mapHashCode(Map<K, V> map)
	{
		int sum = 0;
		int product = 1;
		for (Entry<K, V> o : map)
		{
			K key = o.getKey();
			V value = o.getValue();
			if (key == null && value == null)
			{
				continue;
			}
			int hash = (key == null ? 0 : key.hashCode()) * 31 + (value == null ? 0 : value.hashCode());
			sum += hash;
			product *= hash;
		}
		return sum * 31 + product;
	}
}
