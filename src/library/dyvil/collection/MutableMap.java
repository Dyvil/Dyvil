package dyvil.collection;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.mutable.ArrayMap;
import dyvil.collection.mutable.HashMap;
import dyvil.collection.mutable.TupleMap;
import dyvil.collection.view.MapView;
import dyvil.lang.LiteralConvertible;
import dyvil.util.Option;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

@LiteralConvertible.FromNil
@LiteralConvertible.FromArray
@LiteralConvertible.FromMap
public interface MutableMap<K, V> extends Map<K, V>
{
	@NonNull
	static <K, V> MutableMap<K, V> apply()
	{
		return new HashMap<>();
	}

	@NonNull
	static <K, V> MutableMap<K, V> withCapacity(int capacity)
	{
		return new HashMap<>(capacity);
	}

	@NonNull
	static <K, V> MutableMap<K, V> singleton(K key, V value)
	{
		return ArrayMap.singleton(key, value);
	}

	@NonNull
	static <K, V> MutableMap<K, V> apply(@NonNull Entry<K, V> entry)
	{
		return TupleMap.apply(entry);
	}

	@NonNull
	@SafeVarargs
	static <K, V> MutableMap<K, V> apply(@NonNull Entry<? extends K, ? extends V> @NonNull ... entries)
	{
		return TupleMap.apply(entries);
	}

	@NonNull
	static <K, V> MutableMap<K, V> apply(K @NonNull [] keys, V @NonNull [] values)
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

	@NonNull
	@Override
	Iterator<Entry<K, V>> iterator();

	@NonNull
	@Override
	Iterator<K> keyIterator();

	@NonNull
	@Override
	Iterator<V> valueIterator();

	@Override
	V get(Object key);

	@Nullable
	@Override
	Entry<K, V> getEntry(Object key);

	@NonNull
	@Override
	Option<V> getOption(Object key);

	// Non-mutating Operations

	@NonNull
	@Override
	default MutableMap<K, V> withEntry(K key, V value)
	{
		MutableMap<K, V> copy = this.copy();
		copy.subscript_$eq(key, value);
		return copy;
	}

	@NonNull
	@Override
	default Map<K, V> withEntry(@NonNull Entry<? extends K, ? extends V> entry)
	{
		return this.withEntry(entry.getKey(), entry.getValue());
	}

	@NonNull
	@Override
	default MutableMap<K, V> union(@NonNull Map<? extends K, ? extends V> map)
	{
		MutableMap<K, V> copy = this.copy();
		copy.putAll(map);
		return copy;
	}

	@NonNull
	@Override
	default MutableMap<K, V> keyRemoved(Object key)
	{
		MutableMap<K, V> copy = this.copy();
		copy.removeKey(key);
		return copy;
	}

	@NonNull
	@Override
	default MutableMap<K, V> removed(Object key, Object value)
	{
		MutableMap<K, V> copy = this.copy();
		copy.remove(key, value);
		return copy;
	}

	@NonNull
	@Override
	default Map<K, V> removed(@NonNull Entry<?, ?> entry)
	{
		return this.removed(entry.getKey(), entry.getValue());
	}

	@NonNull
	@Override
	default MutableMap<K, V> valueRemoved(Object value)
	{
		MutableMap<K, V> copy = this.copy();
		copy.removeValue(value);
		return copy;
	}

	@NonNull
	@Override
	default MutableMap<K, V> difference(@NonNull Map<?, ?> map)
	{
		MutableMap<K, V> copy = this.copy();
		copy.removeAll(map);
		return copy;
	}

	@NonNull
	@Override
	default MutableMap<K, V> keyDifference(@NonNull Collection<?> keys)
	{
		MutableMap<K, V> copy = this.copy();
		copy.removeKeys(keys);
		return copy;
	}

	@Override
	default <NK> MutableMap<NK, V> keyMapped(@NonNull Function<? super K, ? extends NK> mapper)
	{
		MutableMap<NK, V> copy = this.emptyCopy();
		for (Entry<K, V> entry : this)
		{
			copy.put(mapper.apply(entry.getKey()), entry.getValue());
		}
		return copy;
	}

	@Override
	default <NK> MutableMap<NK, V> keyMapped(@NonNull BiFunction<? super K, ? super V, ? extends NK> mapper)
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
	default <NV> MutableMap<K, NV> valueMapped(@NonNull Function<? super V, ? extends NV> mapper)
	{
		MutableMap<K, NV> copy = this.emptyCopy();
		for (Entry<K, V> entry : this)
		{
			copy.put(entry.getKey(), mapper.apply(entry.getValue()));
		}
		return copy;
	}

	@Override
	default <NV> MutableMap<K, NV> valueMapped(@NonNull BiFunction<? super K, ? super V, ? extends NV> mapper)
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
	default <NK, NV> MutableMap<NK, NV> entryMapped(@NonNull BiFunction<? super K, ? super V, ? extends @NonNull Entry<? extends NK, ? extends NV>> mapper)
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
	default <NK, NV> MutableMap<NK, NV> flatMapped(@NonNull BiFunction<? super K, ? super V, ? extends @NonNull Iterable<? extends @NonNull Entry<? extends NK, ? extends NV>>> mapper)
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

	@NonNull
	@Override
	default MutableMap<K, V> filtered(@NonNull BiPredicate<? super K, ? super V> condition)
	{
		MutableMap<K, V> copy = this.copy();
		copy.filter(condition);
		return copy;
	}

	@NonNull
	@Override
	default MutableMap<K, V> filteredByKey(@NonNull Predicate<? super K> condition)
	{
		MutableMap<K, V> copy = this.copy();
		copy.filterByKey(condition);
		return copy;
	}

	@NonNull
	@Override
	default MutableMap<K, V> filteredByValue(@NonNull Predicate<? super V> condition)
	{
		MutableMap<K, V> copy = this.copy();
		copy.filterByValue(condition);
		return copy;
	}

	@NonNull
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

	@Nullable
	@Override
	V put(K key, V value);

	@Nullable
	@Override
	default V put(@NonNull Entry<? extends K, ? extends V> entry)
	{
		return this.put(entry.getKey(), entry.getValue());
	}

	@Override
	default void putAll(@NonNull Map<? extends K, ? extends V> map)
	{
		for (Entry<? extends K, ? extends V> entry : map)
		{
			this.put(entry);
		}
	}

	@Nullable
	@Override
	V putIfAbsent(K key, V value);

	@Nullable
	@Override
	default V putIfAbsent(@NonNull Entry<? extends K, ? extends V> entry)
	{
		return this.putIfAbsent(entry.getKey(), entry.getValue());
	}

	@Override
	boolean replace(K key, V oldValue, V newValue);

	@Nullable
	@Override
	V replace(K key, V newValue);

	@Nullable
	@Override
	default V replace(@NonNull Entry<? extends K, ? extends V> entry)
	{
		return this.replace(entry.getKey(), entry.getValue());
	}

	@Nullable
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

	@Nullable
	@Override
	V removeKey(Object key);

	@Override
	boolean removeValue(Object value);

	@Override
	boolean remove(Object key, Object value);

	@Override
	default boolean remove(@NonNull Entry<?, ?> entry)
	{
		return this.remove(entry.getKey(), entry.getValue());
	}

	@Override
	default boolean removeKeys(@NonNull Collection<?> keys)
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
	default boolean removeAll(@NonNull Map<?, ?> map)
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
	default void mapKeys(@NonNull Function<? super K, ? extends K> mapper)
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
	default void mapKeys(@NonNull BiFunction<? super K, ? super V, ? extends K> mapper)
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
	default void mapValues(@NonNull Function<? super V, ? extends V> mapper)
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
	default void mapValues(@NonNull BiFunction<? super K, ? super V, ? extends V> mapper)
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
	default void mapEntries(@NonNull BiFunction<? super K, ? super V, ? extends @NonNull Entry<? extends K, ? extends V>> mapper)
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
	default void flatMap(@NonNull BiFunction<? super K, ? super V, ? extends @NonNull Iterable<? extends @NonNull Entry<? extends K, ? extends V>>> mapper)
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
	void filter(@NonNull BiPredicate<? super K, ? super V> condition);

	@Override
	default void filterByKey(@NonNull Predicate<? super K> condition)
	{
		this.filter((k, v) -> condition.test(k));
	}

	@Override
	default void filterByValue(@NonNull Predicate<? super V> condition)
	{
		this.filter((k, v) -> condition.test(v));
	}

	// Copying

	@NonNull
	@Override
	MutableMap<K, V> copy();

	@NonNull
	@Override
	default MutableMap<K, V> mutable()
	{
		return this;
	}

	@NonNull
	@Override
	default MutableMap<K, V> mutableCopy()
	{
		return this.copy();
	}

	@Override
	@NonNull <NK, NV> MutableMap<NK, NV> emptyCopy();

	@Override
	default <RK, RV> @NonNull MutableMap<RK, RV> emptyCopy(int capacity)
	{
		return this.emptyCopy();
	}

	@NonNull
	@Override
	ImmutableMap<K, V> immutable();

	@Override
	<RK, RV> ImmutableMap.@NonNull Builder<RK, RV> immutableBuilder();

	@Override
	default <RK, RV> ImmutableMap.@NonNull Builder<RK, RV> immutableBuilder(int capacity)
	{
		return this.immutableBuilder();
	}

	@NonNull
	@Override
	default ImmutableMap<K, V> immutableCopy()
	{
		return this.immutable();
	}

	@NonNull
	@Override
	default ImmutableMap<K, V> view()
	{
		return new MapView<>(this);
	}

	@Override
	java.util.@NonNull Map<K, V> toJava();
}
