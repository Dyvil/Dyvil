package dyvil.collection;

import dyvil.collection.mutable.ArrayMap;
import dyvil.collection.mutable.HashMap;
import dyvil.collection.mutable.TupleMap;
import dyvil.collection.view.MapView;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.ColonConvertible;
import dyvil.lang.literal.MapConvertible;
import dyvil.lang.literal.NilConvertible;
import dyvil.util.Option;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

@NilConvertible
@ArrayConvertible
@ColonConvertible(methodName = "singleton")
@MapConvertible
public interface MutableMap<K, V> extends Map<K, V>
{
	static <K, V> MutableMap<K, V> apply()
	{
		return new HashMap<>();
	}

	static <K, V> MutableMap<K, V> withCapacity(int capacity)
	{
		return new HashMap<>(capacity);
	}

	static <K, V> MutableMap<K, V> singleton(K key, V value)
	{
		return ArrayMap.singleton(key, value);
	}

	static <K, V> MutableMap<K, V> apply(Entry<K, V> entry)
	{
		return TupleMap.apply(entry);
	}

	@SafeVarargs
	static <K, V> MutableMap<K, V> apply(Entry<? extends K, ? extends V>... entries)
	{
		return TupleMap.apply(entries);
	}

	static <K, V> MutableMap<K, V> apply(K[] keys, V[] values)
	{
		return new ArrayMap<>(keys, values, true);
	}

	// Simple Getters

	@Override
	default boolean isImmutable()
	{
		return false;
	}

	@Override
	int size();

	@Override
	Iterator<Entry<K, V>> iterator();

	@Override
	Iterator<K> keyIterator();

	@Override
	Iterator<V> valueIterator();

	@Override
	V get(Object key);

	@Override
	Option<V> getOption(Object key);

	// Non-mutating Operations

	@Override
	default MutableMap<K, V> withEntry(K key, V value)
	{
		MutableMap<K, V> copy = this.copy();
		copy.subscript_$eq(key, value);
		return copy;
	}

	@Override
	default Map<K, V> withEntry(Entry<? extends K, ? extends V> entry)
	{
		return this.withEntry(entry.getKey(), entry.getValue());
	}

	@Override
	default MutableMap<K, V> union(Map<? extends K, ? extends V> map)
	{
		MutableMap<K, V> copy = this.copy();
		copy.putAll(map);
		return copy;
	}

	@Override
	default MutableMap<K, V> keyRemoved(Object key)
	{
		MutableMap<K, V> copy = this.copy();
		copy.removeKey(key);
		return copy;
	}

	@Override
	default MutableMap<K, V> removed(Object key, Object value)
	{
		MutableMap<K, V> copy = this.copy();
		copy.remove(key, value);
		return copy;
	}

	@Override
	default Map<K, V> removed(Entry<?, ?> entry)
	{
		return this.removed(entry.getKey(), entry.getValue());
	}

	@Override
	default MutableMap<K, V> valueRemoved(Object value)
	{
		MutableMap<K, V> copy = this.copy();
		copy.removeValue(value);
		return copy;
	}

	@Override
	default MutableMap<K, V> difference(Map<?, ?> map)
	{
		MutableMap<K, V> copy = this.copy();
		copy.removeAll(map);
		return copy;
	}

	@Override
	default MutableMap<K, V> keyDifference(Collection<?> keys)
	{
		MutableMap<K, V> copy = this.copy();
		copy.removeKeys(keys);
		return copy;
	}

	@Override
	default <NK> MutableMap<NK, V> keyMapped(Function<? super K, ? extends NK> mapper)
	{
		MutableMap<NK, V> copy = this.emptyCopy();
		for (Entry<K, V> entry : this)
		{
			copy.put(mapper.apply(entry.getKey()), entry.getValue());
		}
		return copy;
	}

	@Override
	default <NK> MutableMap<NK, V> keyMapped(BiFunction<? super K, ? super V, ? extends NK> mapper)
	{
		MutableMap<NK, V> copy = this.emptyCopy();
		for (Entry<K, V> entry : this)
		{
			V value = entry.getValue();
			copy.put(mapper.apply(entry.getKey(), value), value);
		}
		return copy;
	}

	@Override
	default <NV> MutableMap<K, NV> valueMapped(Function<? super V, ? extends NV> mapper)
	{
		MutableMap<K, NV> copy = this.emptyCopy();
		for (Entry<K, V> entry : this)
		{
			copy.put(entry.getKey(), mapper.apply(entry.getValue()));
		}
		return copy;
	}

	@Override
	default <NV> MutableMap<K, NV> valueMapped(BiFunction<? super K, ? super V, ? extends NV> mapper)
	{
		MutableMap<K, NV> copy = this.emptyCopy();
		for (Entry<K, V> entry : this)
		{
			K key = entry.getKey();
			copy.put(key, mapper.apply(key, entry.getValue()));
		}
		return copy;
	}

	@Override
	default <NK, NV> MutableMap<NK, NV> entryMapped(BiFunction<? super K, ? super V, ? extends Entry<? extends NK, ? extends NV>> mapper)
	{
		MutableMap<NK, NV> copy = this.emptyCopy();
		for (Entry<K, V> entry : this)
		{
			Entry<? extends NK, ? extends NV> newEntry = mapper.apply(entry.getKey(), entry.getValue());
			if (newEntry != null)
			{
				copy.put(newEntry);
			}
		}
		return copy;
	}

	@Override
	default <NK, NV> MutableMap<NK, NV> flatMapped(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends NK, ? extends NV>>> mapper)
	{
		MutableMap<NK, NV> copy = this.emptyCopy();
		for (Entry<K, V> entry : this)
		{
			for (Entry<? extends NK, ? extends NV> newEntry : mapper.apply(entry.getKey(), entry.getValue()))
			{
				copy.put(newEntry);
			}
		}
		return copy;
	}

	@Override
	default MutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		MutableMap<K, V> copy = this.copy();
		copy.filter(condition);
		return copy;
	}

	@Override
	default MutableMap<K, V> filteredByKey(Predicate<? super K> condition)
	{
		MutableMap<K, V> copy = this.copy();
		copy.filterByKey(condition);
		return copy;
	}

	@Override
	default MutableMap<K, V> filteredByValue(Predicate<? super V> condition)
	{
		MutableMap<K, V> copy = this.copy();
		copy.filterByValue(condition);
		return copy;
	}

	@Override
	default MutableMap<V, K> inverted()
	{
		MutableMap<V, K> map = this.emptyCopy();
		for (Entry<K, V> entry : this)
		{
			map.put(entry.getValue(), entry.getKey());
		}
		return map;
	}

	// Mutating Operations

	@Override
	void clear();

	@Override
	default void subscript_$eq(K key, V value)
	{
		this.put(key, value);
	}

	@Override
	V put(K key, V value);

	@Override
	default V put(Entry<? extends K, ? extends V> entry)
	{
		return this.put(entry.getKey(), entry.getValue());
	}

	@Override
	default void putAll(Map<? extends K, ? extends V> map)
	{
		for (Entry<? extends K, ? extends V> entry : map)
		{
			this.put(entry);
		}
	}

	@Override
	V putIfAbsent(K key, V value);

	@Override
	default V putIfAbsent(Entry<? extends K, ? extends V> entry)
	{
		return this.putIfAbsent(entry.getKey(), entry.getValue());
	}

	@Override
	boolean replace(K key, V oldValue, V newValue);

	@Override
	V replace(K key, V newValue);

	@Override
	default V replace(Entry<? extends K, ? extends V> entry)
	{
		return this.replace(entry.getKey(), entry.getValue());
	}

	@Override
	default V remap(Object key, K newKey)
	{
		final V value = this.removeKey(key);
		if (value != null)
		{
			this.put(newKey, value);
		}
		return value;
	}

	@Override
	V removeKey(Object key);

	@Override
	boolean removeValue(Object value);

	@Override
	boolean remove(Object key, Object value);

	@Override
	default boolean remove(Entry<?, ?> entry)
	{
		return this.remove(entry.getKey(), entry.getValue());
	}

	@Override
	default boolean removeKeys(Collection<?> keys)
	{
		boolean removed = false;
		for (Object key : keys)
		{
			if (this.removeKey(key) != null)
			{
				removed = true;
			}
		}
		return removed;
	}

	@Override
	default boolean removeAll(Map<?, ?> map)
	{
		boolean removed = false;
		for (Entry<?, ?> entry : map)
		{
			if (this.remove(entry))
			{
				removed = true;
			}
		}
		return removed;
	}

	@Override
	default void mapKeys(Function<? super K, ? extends K> mapper)
	{
		final int size = this.size();
		final Entry<K, V>[] entries = this.toArray();

		this.clear();
		for (int i = 0; i < size; i++)
		{
			final Entry<K, V> entry = entries[i];
			this.put(mapper.apply(entry.getKey()), entry.getValue());
		}
	}

	@Override
	default void mapKeys(BiFunction<? super K, ? super V, ? extends K> mapper)
	{
		final int size = this.size();
		final Entry<K, V>[] entries = this.toArray();

		this.clear();
		for (int i = 0; i < size; i++)
		{
			final Entry<K, V> entry = entries[i];
			final V value = entry.getValue();
			this.put(mapper.apply(entry.getKey(), value), value);
		}
	}

	@Override
	default void mapValues(Function<? super V, ? extends V> mapper)
	{
		final int size = this.size();
		final Entry<K, V>[] entries = this.toArray();

		this.clear();
		for (int i = 0; i < size; i++)
		{
			final Entry<K, V> entry = entries[i];
			this.put(entry.getKey(), mapper.apply(entry.getValue()));
		}
	}

	@Override
	default void mapValues(BiFunction<? super K, ? super V, ? extends V> mapper)
	{
		final int size = this.size();
		final Entry<K, V>[] entries = this.toArray();

		this.clear();
		for (int i = 0; i < size; i++)
		{
			final Entry<K, V> entry = entries[i];
			final K key = entry.getKey();
			this.put(key, mapper.apply(key, entry.getValue()));
		}
	}

	@Override
	default void mapEntries(BiFunction<? super K, ? super V, ? extends Entry<? extends K, ? extends V>> mapper)
	{
		final int size = this.size();
		final Entry<K, V>[] entries = this.toArray();

		this.clear();
		for (int i = 0; i < size; i++)
		{
			final Entry<K, V> entry = entries[i];
			final Entry<? extends K, ? extends V> newEntry = mapper.apply(entry.getKey(), entry.getValue());
			if (newEntry != null)
			{
				this.put(newEntry);
			}
		}
	}

	@Override
	default void flatMap(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends K, ? extends V>>> mapper)
	{
		final Entry<K, V>[] entries = this.toArray();

		this.clear();
		for (Entry<K, V> entry : entries)
		{
			for (Entry<? extends K, ? extends V> newEntry : mapper.apply(entry.getKey(), entry.getValue()))
			{
				this.put(newEntry);
			}
		}
	}

	@Override
	void filter(BiPredicate<? super K, ? super V> condition);

	@Override
	default void filterByKey(Predicate<? super K> condition)
	{
		this.filter((k, v) -> condition.test(k));
	}

	@Override
	default void filterByValue(Predicate<? super V> condition)
	{
		this.filter((k, v) -> condition.test(v));
	}

	// Copying

	@Override
	MutableMap<K, V> copy();

	@Override
	default MutableMap<K, V> mutable()
	{
		return this;
	}

	@Override
	default MutableMap<K, V> mutableCopy()
	{
		return this.copy();
	}

	@Override
	<NK, NV> MutableMap<NK, NV> emptyCopy();

	@Override
	default <RK, RV> MutableMap<RK, RV> emptyCopy(int capacity)
	{
		return this.emptyCopy();
	}

	@Override
	ImmutableMap<K, V> immutable();

	@Override
	<RK, RV> ImmutableMap.Builder<RK, RV> immutableBuilder();

	@Override
	default <RK, RV> ImmutableMap.Builder<RK, RV> immutableBuilder(int capacity)
	{
		return this.immutableBuilder();
	}

	@Override
	default ImmutableMap<K, V> immutableCopy()
	{
		return this.immutable();
	}

	@Override
	default ImmutableMap<K, V> view()
	{
		return new MapView<>(this);
	}

	@Override
	java.util.Map<K, V> toJava();
}
