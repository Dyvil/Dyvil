package dyvil.collection;

import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.MapConvertible;
import dyvil.lang.literal.NilConvertible;
import dyvil.tuple.Tuple2;
import dyvil.util.None;
import dyvil.util.Option;
import dyvil.util.Some;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@NilConvertible(methodName = "fromNil")
@ArrayConvertible
@MapConvertible
public interface Map<K, V> extends Iterable<Entry<K, V>>, Serializable
{
	static <K, V> ImmutableMap<K, V> fromNil()
	{
		return ImmutableMap.apply();
	}
	
	static <K, V> MutableMap<K, V> apply()
	{
		return MutableMap.apply();
	}
	
	static <K, V> ImmutableMap<K, V> apply(Entry<K, V> entry)
	{
		return ImmutableMap.apply(entry);
	}
	
	static <K, V> ImmutableMap<K, V> apply(
			Tuple2<? extends K, ? extends V>... entries)
	{
		return ImmutableMap.apply(entries);
	}
	
	static <K, V> ImmutableMap<K, V> apply(K[] keys, V[] values)
	{
		return ImmutableMap.apply(keys, values);
	}
	
	// Simple Getters
	
	boolean isImmutable();
	
	/**
	 * Returns the size of this map, i.e. the number of mappings contained in
	 * this map.
	 */
	int size();
	
	/**
	 * Returns true if and if only this map contains no mappings. The standard
	 * implementation defines a map as empty if it's size as calculated by
	 * {@link #size()} is exactly {@code 0}.
	 *
	 * @return true, if this map contains no mappings
	 */
	default boolean isEmpty()
	{
		return this.size() == 0;
	}
	
	default boolean isSorted()
	{
		if (this.size() < 2)
		{
			return true;
		}
		
		return Collection.iteratorSorted(this.keyIterator());
	}
	
	default boolean isSorted(Comparator<? super K> comparator)
	{
		if (this.size() < 2)
		{
			return true;
		}
		
		return Collection.iteratorSorted(this.keyIterator(), comparator);
	}
	
	/**
	 * Creates and returns an {@link Iterator} over the mappings of this map,
	 * packed in {@linkplain Entry Tuples} containing the key as their first
	 * value and the value as their second value.
	 *
	 * @return an iterator over the mappings of this map
	 */
	@Override
	Iterator<Entry<K, V>> iterator();
	
	/**
	 * Creates and returns an {@link Spliterator} over the mappings of this map,
	 * packed in {@linkplain Entry Tuples} containing the key as their first
	 * value and the value as their second value.
	 *
	 * @return an iterator over the mappings of this map
	 */
	@Override
	default Spliterator<Entry<K, V>> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), 0);
	}
	
	default Stream<Entry<K, V>> stream()
	{
		return StreamSupport.stream(this.spliterator(), false);
	}
	
	default Stream<Entry<K, V>> parallelStream()
	{
		return StreamSupport.stream(this.spliterator(), true);
	}
	
	default Iterable<K> keys()
	{
		return new Iterable<K>()
		{
			@Override
			public Iterator<K> iterator()
			{
				return Map.this.keyIterator();
			}
			
			@Override
			public void forEach(Consumer<? super K> action)
			{
				Map.this.forEachKey(action);
			}
			
			@Override
			public Spliterator<K> spliterator()
			{
				return Map.this.keySpliterator();
			}
		};
	}
	
	Iterator<K> keyIterator();
	
	default Spliterator<K> keySpliterator()
	{
		return Spliterators.spliterator(this.keyIterator(), this.size(), 0);
	}
	
	default Stream<K> keyStream()
	{
		return StreamSupport.stream(this.keySpliterator(), false);
	}
	
	default Stream<K> parallelKeyStream()
	{
		return StreamSupport.stream(this.keySpliterator(), true);
	}
	
	default Iterable<V> values()
	{
		return new Iterable<V>()
		{
			@Override
			public Iterator<V> iterator()
			{
				return Map.this.valueIterator();
			}
			
			@Override
			public void forEach(Consumer<? super V> action)
			{
				Map.this.forEachValue(action);
			}
			
			@Override
			public Spliterator<V> spliterator()
			{
				return Map.this.valueSpliterator();
			}
		};
	}
	
	Iterator<V> valueIterator();
	
	default Spliterator<V> valueSpliterator()
	{
		return Spliterators.spliterator(this.valueIterator(), this.size(), 0);
	}
	
	default Stream<V> valueStream()
	{
		return StreamSupport.stream(this.valueSpliterator(), false);
	}
	
	default Stream<V> parallelValueStream()
	{
		return StreamSupport.stream(this.valueSpliterator(), true);
	}
	
	@Override
	default void forEach(Consumer<? super Entry<K, V>> action)
	{
		for (Entry<K, V> entry : this)
		{
			action.accept(entry);
		}
	}
	
	default void forEach(BiConsumer<? super K, ? super V> action)
	{
		for (Entry<K, V> entry : this)
		{
			action.accept(entry.getKey(), entry.getValue());
		}
	}
	
	default void forEachKey(Consumer<? super K> action)
	{
		for (Iterator<K> iterator = this.keyIterator(); iterator.hasNext(); )
		{
			action.accept(iterator.next());
		}
	}
	
	default void forEachValue(Consumer<? super V> action)
	{
		for (Iterator<V> iterator = this.valueIterator(); iterator.hasNext(); )
		{
			action.accept(iterator.next());
		}
	}
	
	default boolean allMatch(BiPredicate<? super K, ? super V> condition)
	{
		for (Entry<K, V> entry : this)
		{
			if (!condition.test(entry.getKey(), entry.getValue()))
			{
				return false;
			}
		}
		return true;
	}
	
	default boolean exists(BiPredicate<? super K, ? super V> condition)
	{
		for (Entry<K, V> entry : this)
		{
			if (condition.test(entry.getKey(), entry.getValue()))
			{
				return true;
			}
		}
		return false;
	}

	default Entry<K, V> find(BiPredicate<? super K, ? super V> condition)
	{
		for (Entry<K, V> entry : this)
		{
			if (condition.test(entry.getKey(), entry.getValue()))
			{
				return entry;
			}
		}

		return null;
	}
	
	/**
	 * Returns true if and if only this map contains a mapping for the given
	 * {@code key}.
	 *
	 * @param key
	 * 		the key
	 *
	 * @return true, if this map contains a mapping for the key
	 */
	default boolean $qmark$at(Object key)
	{
		return this.containsKey(key);
	}
	
	/**
	 * Returns true if and if only this map contains a mapping that maps the
	 * given {@code key} to the given {@code value}.
	 *
	 * @param key
	 * 		the key
	 * @param value
	 * 		the value
	 *
	 * @return true, if this map contains a mapping for the key and the value
	 */
	default boolean $qmark(Object key, Object value)
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
	 * 		the entry
	 *
	 * @return true, if this map contains the mapping represented by the entry
	 */
	default boolean $qmark(Entry<?, ?> entry)
	{
		return this.contains(entry.getKey(), entry.getValue());
	}
	
	/**
	 * Returns true if and if only this map contains a mapping to the given
	 * {@code value}.
	 *
	 * @param value
	 * 		the value
	 *
	 * @return true, if this map contains a mapping to the value
	 */
	default boolean $qmark$colon(Object value)
	{
		return this.containsValue(value);
	}
	
	/**
	 * Returns true if and if only this map contains a mapping for the given
	 * {@code key}.
	 *
	 * @param key
	 * 		the key
	 *
	 * @return true, if this map contains a mapping for the key
	 */
	default boolean containsKey(Object key)
	{
		for (Iterator<K> keyIterator = this.keyIterator(); keyIterator
				.hasNext(); )
		{
			if (Objects.equals(key, keyIterator.next()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Returns true if and if only this map contains a mapping to the given
	 * {@code value}.
	 *
	 * @param value
	 * 		the value
	 *
	 * @return true, if this map contains a mapping to the value
	 */
	default boolean containsValue(Object value)
	{
		for (Iterator<V> valueIterator = this.valueIterator(); valueIterator
				.hasNext(); )
		{
			if (Objects.equals(value, valueIterator.next()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Returns true if and if only this map contains a mapping that maps the
	 * given {@code key} to the given {@code value}.
	 *
	 * @param key
	 * 		the key
	 * @param value
	 * 		the value
	 *
	 * @return true, if this map contains a mapping for the key and the value
	 */
	default boolean contains(Object key, Object value)
	{
		for (Entry<K, V> entry : this)
		{
			if (Objects.equals(key, entry.getKey()))
			{
				return Objects.equals(value, entry.getValue());
			}
		}
		
		return false;
	}
	
	/**
	 * Returns true if and if only this map contains a mapping that maps the
	 * key, as given by the first value of the {@code entry} to the value, as
	 * given by the second value of the {@code entry}. The default
	 * implementation of this method delegates to the {@link $qmark(Object,
	 * Object)} method.
	 *
	 * @param entry
	 * 		the entry
	 *
	 * @return true, if this map contains the mapping represented by the entry
	 */
	default boolean contains(Entry<?, ?> entry)
	{
		return this.contains(entry.getKey(), entry.getValue());
	}
	
	/**
	 * Gets and returns the value for the given {@code key}. If no mapping for
	 * the {@code key} exists, {@code null} is returned. This alias forwarder
	 * for Dyvil Subscript Syntax delegates to {@link #get(Object)}.
	 *
	 * @param key
	 * 		the key
	 *
	 * @return the value
	 */
	default V subscript(Object key)
	{
		return this.get(key);
	}
	
	/**
	 * Gets and returns the value for the given {@code key}. If no mapping for
	 * the {@code key} exists, {@code null} is returned.
	 *
	 * @param key
	 * 		the key
	 *
	 * @return the value
	 */
	V get(Object key);
	
	/**
	 * Gets and returns an optional value for the given {@code key}. If no
	 * mapping for the {@code key} exists, {@link None} is returned, otherwise,
	 * the mapped value is wrapped in a {@link Some}.
	 *
	 * @param key
	 * 		the key
	 *
	 * @return an option containing the value, or None if not mapping exists for
	 * the key
	 */
	Option<V> getOption(Object key);
	
	// Non-mutating Operations
	
	/**
	 * Returns a map that contains all entries of this map plus the new entry
	 * specified by {@code key} and {@code value} as if it were added by {@link
	 * #subscript_$eq(Object, Object)}. If the {@code key} is already present in
	 * this map, a map is returned that uses the given {@code value} instead of
	 * the previous value for the {@code key}, and that has the same size as
	 * this map.
	 *
	 * @param key
	 * 		the key
	 * @param value
	 * 		the value
	 *
	 * @return a map that contains all entries of this map plus the new entry
	 */
	Map<K, V> $plus(K key, V value);
	
	/**
	 * Returns a map that contains all entries of this map plus the new {@code
	 * entry}, as if it were added by {@link #subscript_$eq(Object, Object)}. If
	 * the {@code key} is already present in this map, a map is returned that
	 * uses the given {@code value} instead of the previous value for the {@code
	 * key}, and that has the same size as this map.
	 *
	 * @param entry
	 * 		the entry
	 *
	 * @return a map that contains all entries of this map plus the new entry
	 *
	 * @see #$plus(Object, Object)
	 */
	Map<K, V> $plus(Entry<? extends K, ? extends V> entry);
	
	/**
	 * Returns a map that contains all entries of this map plus all entries of
	 * the given {@code map}, as if they were added by {@link
	 * #subscript_$eq(Object, Object)}. If a key in the given map is already
	 * present in this map, a map is returned that uses the value from the given
	 * {@code map} for that key.
	 *
	 * @param map
	 *
	 * @return
	 */
	Map<K, V> $plus$plus(Map<? extends K, ? extends V> map);
	
	Map<K, V> $minus$at(Object key);
	
	Map<K, V> $minus(Object key, Object value);
	
	Map<K, V> $minus(Entry<?, ?> entry);
	
	Map<K, V> $minus$colon(Object value);
	
	/**
	 * Returns a new map containing all entries of this map minus the entries of
	 * the given {@code map}. An entry of this map is not retained if the given
	 * map contains the exact entry, which means both key and value have to
	 * match. If entries should be removed based on keys only (ignoring values),
	 * {@link #$minus$minus(Collection) --} should be used instead.
	 *
	 * @param map
	 * 		the map whose entries should not be present in the resulting map
	 *
	 * @return a map that contains all entries of this map minus the entries of
	 * the given map
	 */
	Map<K, V> $minus$minus(Map<?, ?> map);
	
	Map<K, V> $minus$minus(Collection<?> keys);
	
	<NK> Map<NK, V> keyMapped(
			BiFunction<? super K, ? super V, ? extends NK> mapper);
	
	<NV> Map<K, NV> valueMapped(
			BiFunction<? super K, ? super V, ? extends NV> mapper);
	
	<NK, NV> Map<NK, NV> entryMapped(
			BiFunction<? super K, ? super V, ? extends Entry<? extends NK, ? extends NV>> mapper);
	
	<NK, NV> Map<NK, NV> flatMapped(
			BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends NK, ? extends NV>>> mapper);
	
	Map<K, V> filtered(BiPredicate<? super K, ? super V> condition);
	
	Map<V, K> inverted();
	
	// Mutating Operations
	
	default void $plus$eq(K key, V value)
	{
		this.put(key, value);
	}
	
	default void $plus$eq(Entry<? extends K, ? extends V> entry)
	{
		this.put(entry.getKey(), entry.getValue());
	}
	
	default void $plus$plus$eq(Map<? extends K, ? extends V> map)
	{
		for (Entry<? extends K, ? extends V> entry : map)
		{
			this.put(entry.getKey(), entry.getValue());
		}
	}
	
	default void $minus$at$eq(Object key)
	{
		this.removeKey(key);
	}
	
	default void $minus$eq(Object key, Object value)
	{
		this.remove(key, value);
	}
	
	default void $minus$eq(Entry<?, ?> entry)
	{
		this.remove(entry.getKey(), entry.getValue());
	}
	
	default void $minus$colon$eq(Object value)
	{
		this.removeValue(value);
	}
	
	default void $minus$minus$eq(Collection<?> keys)
	{
		for (Object key : keys)
		{
			this.removeKey(key);
		}
	}
	
	default void $minus$minus$eq(Map<?, ?> map)
	{
		for (Entry<?, ?> entry : map)
		{
			this.remove(entry);
		}
	}
	
	void clear();
	
	void subscript_$eq(K key, V value);
	
	V put(K key, V value);
	
	V put(Entry<? extends K, ? extends V> entry);
	
	boolean putIfAbsent(K key, V value);
	
	boolean putIfAbsent(Entry<? extends K, ? extends V> entry);
	
	void putAll(Map<? extends K, ? extends V> map);
	
	boolean replace(K key, V oldValue, V newValue);
	
	V replace(K key, V newValue);
	
	V replace(Entry<? extends K, ? extends V> entry);
	
	V removeKey(Object key);
	
	boolean removeValue(Object value);
	
	boolean remove(Object key, Object value);
	
	boolean remove(Entry<?, ?> entry);
	
	boolean removeKeys(Collection<?> keys);
	
	boolean removeAll(Map<?, ?> map);
	
	void mapKeys(BiFunction<? super K, ? super V, ? extends K> mapper);
	
	void mapValues(BiFunction<? super K, ? super V, ? extends V> mapper);
	
	void mapEntries(
			BiFunction<? super K, ? super V, ? extends Entry<? extends K, ? extends V>> mapper);
	
	void flatMap(
			BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends K, ? extends V>>> mapper);
	
	void filter(BiPredicate<? super K, ? super V> condition);
	
	// Arrays
	
	default Entry<K, V>[] toArray()
	{
		Entry<K, V>[] array = new Entry[this.size()];
		this.toArray(0, array);
		return array;
	}
	
	default void toArray(Entry<K, V>[] store)
	{
		this.toArray(0, store);
	}
	
	default void toArray(int index, Entry<K, V>[] store)
	{
		for (Entry<K, V> entry : this)
		{
			store[index++] = entry;
		}
	}
	
	default Object[] toKeyArray()
	{
		Object[] array = new Object[this.size()];
		this.toKeyArray(0, array);
		return array;
	}
	
	default K[] toKeyArray(Class<K> type)
	{
		K[] array = (K[]) Array.newInstance(type, this.size());
		this.toKeyArray(0, array);
		return array;
	}
	
	default void toKeyArray(Object[] store)
	{
		this.toKeyArray(0, store);
	}
	
	default void toKeyArray(int index, Object[] store)
	{
		for (Iterator<K> iterator = this.keyIterator(); iterator.hasNext(); )
		{
			store[index++] = iterator.next();
		}
	}
	
	default Object[] toValueArray()
	{
		Object[] array = new Object[this.size()];
		this.toValueArray(0, array);
		return array;
	}
	
	default V[] toValueArray(Class<V> type)
	{
		V[] array = (V[]) Array.newInstance(type, this.size());
		this.toValueArray(0, array);
		return array;
	}
	
	default void toValueArray(Object[] store)
	{
		this.toValueArray(0, store);
	}
	
	default void toValueArray(int index, Object[] store)
	{
		for (Iterator<V> iterator = this.valueIterator(); iterator.hasNext(); )
		{
			store[index++] = iterator.next();
		}
	}
	
	// Copying and Views
	
	Map<K, V> copy();
	
	MutableMap<K, V> mutable();
	
	MutableMap<K, V> mutableCopy();
	
	ImmutableMap<K, V> immutable();
	
	ImmutableMap<K, V> immutableCopy();
	
	ImmutableMap<K, V> view();
	
	/**
	 * Returns the Java Collection Framework equivalent of this map. The
	 * returned map is not a view of this one, but an exact copy. Immutable maps
	 * should return a map that is locked for mutation, which is usually ensured
	 * by wrapping the map with {@link java.util.Collections#unmodifiableMap(java.util.Map)
	 * Collections.unmodifiableMap}.
	 *
	 * @return a java collection containing the elements of this collection
	 */
	java.util.Map<K, V> toJava();
	
	// toString, equals and hashCode
	
	@Override
	String toString();
	
	@Override
	boolean equals(Object obj);
	
	@Override
	int hashCode();
	
	static <K, V> String mapToString(Map<K, V> map)
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
			builder.append(entry.getKey()).append(" -> ")
					.append(entry.getValue());
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
	
	static <K, V> boolean mapEquals(Map<K, V> map, Object obj)
	{
		if (!(obj instanceof Map))
		{
			return false;
		}
		
		return mapEquals(map, (Map) obj);
	}
	
	static <K, V> boolean mapEquals(Map<K, V> map1, Map<K, V> map2)
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
	
	static <K, V> int mapHashCode(Map<K, V> map)
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
			int hash =
					(key == null ? 0 : key.hashCode()) * 31 + (value == null ?
							0 :
							value.hashCode());
			sum += hash;
			product *= hash;
		}
		return sum * 31 + product;
	}
	
	default String toString(String prefix, String entrySeparator,
			String keyValueSeparator, String postfix)
	{
		StringBuilder builder = new StringBuilder();
		this.toString(builder, prefix, entrySeparator, keyValueSeparator,
				postfix);
		return builder.toString();
	}
	
	default void toString(StringBuilder builder, String prefix,
			String entrySeparator, String keyValueSeparator, String postfix)
	{
		builder.append(prefix);
		if (this.isEmpty())
		{
			builder.append(postfix);
			return;
		}
		
		Iterator<Entry<K, V>> iterator = this.iterator();
		Entry<K, V> first = iterator.next();
		builder.append(first.getKey()).append(keyValueSeparator)
				.append(first.getValue());
		while (iterator.hasNext())
		{
			first = iterator.next();
			builder.append(entrySeparator).append(first.getKey())
					.append(keyValueSeparator).append(first.getValue());
		}
		builder.append(postfix);
	}
}
