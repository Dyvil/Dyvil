package dyvil.collection;

import dyvil.collection.impl.MapKeys;
import dyvil.collection.impl.MapValues;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.ColonConvertible;
import dyvil.lang.literal.MapConvertible;
import dyvil.lang.literal.NilConvertible;
import dyvil.ref.ObjectRef;
import dyvil.util.None;
import dyvil.util.Option;
import dyvil.util.Some;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@NilConvertible(methodName = "empty")
@ColonConvertible(methodName = "singleton")
@ArrayConvertible
@MapConvertible
public interface Map<K, V> extends Iterable<Entry<K, V>>, Serializable
{
	static <K, V> ImmutableMap<K, V> empty()
	{
		return ImmutableMap.apply();
	}

	static <K, V> MutableMap<K, V> apply()
	{
		return MutableMap.apply();
	}

	static <K, V> ImmutableMap<K, V> singleton(K key, V value)
	{
		return ImmutableMap.singleton(key, value);
	}

	static <K, V> ImmutableMap<K, V> apply(Entry<K, V> entry)
	{
		return ImmutableMap.apply(entry);
	}

	@SafeVarargs
	static <K, V> ImmutableMap<K, V> apply(Entry<? extends K, ? extends V>... entries)
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
	 * Returns the size of this map, i.e. the number of mappings contained in this map.
	 */
	int size();

	/**
	 * Returns true if and if only this map contains no mappings. The standard implementation defines a map as empty if
	 * it's size as calculated by {@link #size()} is exactly {@code 0}.
	 *
	 * @return true, if this map contains no mappings
	 */
	default boolean isEmpty()
	{
		return this.size() == 0;
	}

	default boolean isSorted()
	{
		return this.size() < 2 || Collection.iteratorSorted(this.keyIterator());
	}

	default boolean isSorted(Comparator<? super K> comparator)
	{
		return this.size() < 2 || Collection.iteratorSorted(this.keyIterator(), comparator);
	}

	/**
	 * Creates and returns an {@link Iterator} over the mappings of this map, packed in {@linkplain Entry Tuples}
	 * containing the key as their first value and the value as their second value.
	 *
	 * @return an iterator over the mappings of this map
	 */
	@Override
	Iterator<Entry<K, V>> iterator();

	/**
	 * Creates and returns an {@link Spliterator} over the mappings of this map, packed in {@linkplain Entry Tuples}
	 * containing the key as their first value and the value as their second value.
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

	default Queryable<K> keys()
	{
		return new MapKeys<>(this);
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

	default Queryable<V> values()
	{
		return new MapValues<>(this);
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
	 * Returns true if and if only this map contains a mapping for the given {@code key}.
	 *
	 * @param key
	 * 	the key
	 *
	 * @return true, if this map contains a mapping for the key
	 */
	default boolean containsKey(Object key)
	{
		for (Iterator<K> keyIterator = this.keyIterator(); keyIterator.hasNext(); )
		{
			if (Objects.equals(key, keyIterator.next()))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns true if and if only this map contains a mapping to the given {@code value}.
	 *
	 * @param value
	 * 	the value
	 *
	 * @return true, if this map contains a mapping to the value
	 */
	default boolean containsValue(Object value)
	{
		for (Iterator<V> valueIterator = this.valueIterator(); valueIterator.hasNext(); )
		{
			if (Objects.equals(value, valueIterator.next()))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns true if and if only this map contains a mapping that maps the given {@code key} to the given {@code
	 * value}.
	 *
	 * @param key
	 * 	the key
	 * @param value
	 * 	the value
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
	 * Returns true if and if only this map contains a mapping that maps the key, as given by the first value of the
	 * {@code entry} to the value, as given by the second value of the {@code entry}. The default implementation of this
	 * method delegates to the {@link #contains(Object, Object)} method.
	 *
	 * @param entry
	 * 	the entry
	 *
	 * @return true, if this map contains the mapping represented by the entry
	 */
	default boolean contains(Entry<?, ?> entry)
	{
		return this.contains(entry.getKey(), entry.getValue());
	}

	/**
	 * Gets and returns the value for the given {@code key}. If no mapping for the {@code key} exists, {@code null} is
	 * returned. This alias forwarder for Dyvil Subscript Syntax delegates to {@link #get(Object)}.
	 *
	 * @param key
	 * 	the key
	 *
	 * @return the value
	 */
	default V subscript(Object key)
	{
		return this.get(key);
	}

	default ObjectRef<V> subscript_$amp(K key)
	{
		return new ObjectRef<V>()
		{
			@Override
			public V get()
			{
				return Map.this.get(key);
			}

			@Override
			public void set(V value)
			{
				Map.this.put(key, value);
			}
		};
	}

	/**
	 * Gets and returns the value for the given {@code key}. If no mapping for the {@code key} exists, {@code null} is
	 * returned.
	 *
	 * @param key
	 * 	the key
	 *
	 * @return the value
	 */
	V get(Object key);

	/**
	 * Gets and returns an optional value for the given {@code key}. If no mapping for the {@code key} exists, {@link
	 * None} is returned, otherwise, the mapped value is wrapped in a {@link Some}.
	 *
	 * @param key
	 * 	the key
	 *
	 * @return an option containing the value, or None if not mapping exists for the key
	 */
	Option<V> getOption(Object key);

	// Non-mutating Operations

	/**
	 * Returns a map that contains all entries of this map plus the new entry specified by {@code key} and {@code value}
	 * as if it were added by {@link #subscript_$eq(Object, Object)}. If the {@code key} is already present in this map,
	 * a map is returned that uses the given {@code value} instead of the previous value for the {@code key}, and that
	 * has the same size as this map.
	 *
	 * @param key
	 * 	the key
	 * @param value
	 * 	the value
	 *
	 * @return a map that contains all entries of this map plus the new entry
	 */
	Map<K, V> withEntry(K key, V value);

	/**
	 * Returns a map that contains all entries of this map plus the new {@code entry}, as if it were added by {@link
	 * #subscript_$eq(Object, Object)}. If the {@code key} is already present in this map, a map is returned that uses
	 * the given {@code value} instead of the previous value for the {@code key}, and that has the same size as this
	 * map.
	 *
	 * @param entry
	 * 	the entry
	 *
	 * @return a map that contains all entries of this map plus the new entry
	 *
	 * @see #withEntry(Object, Object)
	 */
	Map<K, V> withEntry(Entry<? extends K, ? extends V> entry);

	/**
	 * Returns a map that contains all entries of this map plus all entries of the given {@code map}, as if they were
	 * added by {@link #subscript_$eq(Object, Object)}. If a key in the given map is already present in this map, a map
	 * is returned that uses the value from the given {@code map} for that key.
	 *
	 * @param map
	 * 	the second map to add entries from
	 *
	 * @return the map that contains all entries of this map plus all entries of the other map
	 */
	Map<K, V> union(Map<? extends K, ? extends V> map);

	Map<K, V> removed(Object key, Object value);

	Map<K, V> removed(Entry<?, ?> entry);

	Map<K, V> keyRemoved(Object key);

	Map<K, V> valueRemoved(Object value);

	/**
	 * Returns a new map containing all entries of this map minus the entries of the given {@code map}. An entry of this
	 * map is not retained if the given map contains the exact entry, which means both key and value have to match. If
	 * entries should be removed based on keys only (ignoring values), {@link #keyDifference(Collection) --} should be
	 * used instead.
	 *
	 * @param map
	 * 	the map whose entries should not be present in the resulting map
	 *
	 * @return a map that contains all entries of this map minus the entries of the given map
	 */
	Map<K, V> difference(Map<?, ?> map);

	Map<K, V> keyDifference(Collection<?> keys);

	<NK> Map<NK, V> keyMapped(Function<? super K, ? extends NK> mapper);

	<NK> Map<NK, V> keyMapped(BiFunction<? super K, ? super V, ? extends NK> mapper);

	<NV> Map<K, NV> valueMapped(Function<? super V, ? extends NV> mapper);

	<NV> Map<K, NV> valueMapped(BiFunction<? super K, ? super V, ? extends NV> mapper);

	<NK, NV> Map<NK, NV> entryMapped(BiFunction<? super K, ? super V, ? extends Entry<? extends NK, ? extends NV>> mapper);

	<NK, NV> Map<NK, NV> flatMapped(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends NK, ? extends NV>>> mapper);

	Map<K, V> filtered(BiPredicate<? super K, ? super V> condition);

	Map<K, V> filteredByKey(Predicate<? super K> condition);

	Map<K, V> filteredByValue(Predicate<? super V> condition);

	Map<V, K> inverted();

	// Mutating Operations

	void clear();

	void subscript_$eq(K key, V value);

	V put(K key, V value);

	V put(Entry<? extends K, ? extends V> entry);

	/**
	 * Inserts the given {@code value} for the {@code key} if no value is currently present for the key in this map.
	 * This method returns the old value if it exists for the given key, and otherwise the given {@code value}. If the
	 * given value cannot be stored in this {@link Map}, {@code null} is returned.
	 *
	 * @param key
	 * 	the key
	 * @param value
	 * 	the value to insert
	 *
	 * @return the old value if it exists for the key; the given value otherwise
	 */
	V putIfAbsent(K key, V value);

	/**
	 * @see #putIfAbsent(Object, Object)
	 */
	V putIfAbsent(Entry<? extends K, ? extends V> entry);

	void putAll(Map<? extends K, ? extends V> map);

	boolean replace(K key, V oldValue, V newValue);

	V replace(K key, V newValue);

	V replace(Entry<? extends K, ? extends V> entry);

	V remap(Object key, K newKey);

	V removeKey(Object key);

	boolean removeValue(Object value);

	boolean remove(Object key, Object value);

	boolean remove(Entry<?, ?> entry);

	boolean removeKeys(Collection<?> keys);

	boolean removeAll(Map<?, ?> map);

	void mapKeys(Function<? super K, ? extends K> mapper);

	void mapKeys(BiFunction<? super K, ? super V, ? extends K> mapper);

	void mapValues(Function<? super V, ? extends V> mapper);

	void mapValues(BiFunction<? super K, ? super V, ? extends V> mapper);

	void mapEntries(BiFunction<? super K, ? super V, ? extends Entry<? extends K, ? extends V>> mapper);

	void flatMap(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends K, ? extends V>>> mapper);

	void filter(BiPredicate<? super K, ? super V> condition);

	void filterByKey(Predicate<? super K> condition);

	void filterByValue(Predicate<? super V> condition);

	// Arrays

	default Entry<K, V>[] toArray()
	{
		Entry<K, V>[] array = (Entry<K, V>[]) new Entry[this.size()];
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

	<RK, RV> MutableMap<RK, RV> emptyCopy();

	<RK, RV> MutableMap<RK, RV> emptyCopy(int capacity);

	MutableMap<K, V> mutable();

	MutableMap<K, V> mutableCopy();

	ImmutableMap<K, V> immutable();

	ImmutableMap<K, V> immutableCopy();

	<RK, RV> ImmutableMap.Builder<RK, RV> immutableBuilder();

	<RK, RV> ImmutableMap.Builder<RK, RV> immutableBuilder(int capacity);

	ImmutableMap<K, V> view();

	/**
	 * Returns the Java Collection Framework equivalent of this map. The returned map is not a view of this one, but an
	 * exact copy. Immutable maps should return a map that is locked for mutation, which is usually ensured by wrapping
	 * the map with {@link java.util.Collections#unmodifiableMap(java.util.Map) Collections.unmodifiableMap}.
	 *
	 * @return a java collection containing the elements of this collection
	 */
	java.util.Map<K, V> toJava();

	// toString, equals and hashCode

	String EMPTY_STRING               = "[]";
	String START_STRING               = "[ ";
	String END_STRING                 = " ]";
	String ENTRY_SEPARATOR_STRING     = ", ";
	String KEY_VALUE_SEPARATOR_STRING = ": ";

	@Override
	String toString();

	default void toString(StringBuilder builder)
	{
		this.toString(builder, START_STRING, ENTRY_SEPARATOR_STRING, KEY_VALUE_SEPARATOR_STRING, END_STRING);
	}

	default String toString(String prefix, String entrySeparator, String keyValueSeparator, String postfix)
	{
		StringBuilder builder = new StringBuilder();
		this.toString(builder, prefix, entrySeparator, keyValueSeparator, postfix);
		return builder.toString();
	}

	default void toString(StringBuilder builder, String prefix, String entrySeparator, String keyValueSeparator, String postfix)
	{
		builder.append(prefix);
		if (this.isEmpty())
		{
			builder.append(postfix);
			return;
		}

		final Iterator<Entry<K, V>> iterator = this.iterator();
		Entry<K, V> entry = iterator.next();

		builder.append(entry.getKey()).append(keyValueSeparator).append(entry.getValue());
		while (iterator.hasNext())
		{
			entry = iterator.next();
			builder.append(entrySeparator).append(entry.getKey()).append(keyValueSeparator).append(entry.getValue());
		}
		builder.append(postfix);
	}

	@Override
	boolean equals(Object obj);

	@Override
	int hashCode();

	static <K, V> String mapToString(Map<K, V> map)
	{
		if (map.isEmpty())
		{
			return EMPTY_STRING;
		}

		final StringBuilder builder = new StringBuilder(START_STRING);
		final Iterator<Entry<K, V>> iterator = map.iterator();
		while (true)
		{
			final Entry<K, V> entry = iterator.next();
			builder.append(entry.getKey()).append(KEY_VALUE_SEPARATOR_STRING).append(entry.getValue());
			if (iterator.hasNext())
			{
				builder.append(ENTRY_SEPARATOR_STRING);
			}
			else
			{
				break;
			}
		}
		return builder.append(END_STRING).toString();
	}

	static <K, V> boolean mapEquals(Map<K, V> map, Object obj)
	{
		return obj instanceof Map && mapEquals(map, (Map<K, V>) obj);
	}

	static <K, V> boolean mapEquals(Map<K, V> map1, Map<K, V> map2)
	{
		if (map1.size() != map2.size())
		{
			return false;
		}

		// One-sided comparison is ok since we check for equal size
		for (Entry<K, V> entry : map1)
		{
			if (!map2.contains(entry))
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
			int hash = (key == null ? 0 : key.hashCode()) * 31 + (value == null ? 0 : value.hashCode());
			sum += hash;
			product *= hash;
		}
		return sum * 31 + product;
	}
}
