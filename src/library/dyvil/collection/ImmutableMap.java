package dyvil.collection;

import dyvil.annotation.Mutating;
import dyvil.annotation.internal.Covariant;
import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.immutable.ArrayMap;
import dyvil.collection.immutable.EmptyMap;
import dyvil.collection.immutable.SingletonMap;
import dyvil.collection.immutable.TupleMap;
import dyvil.lang.LiteralConvertible;
import dyvil.util.ImmutableException;
import dyvil.util.Option;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

@LiteralConvertible.FromNil
@LiteralConvertible.FromArray
@LiteralConvertible.FromColonOperator(methodName = "singleton")
@LiteralConvertible.FromMap
public interface ImmutableMap<@Covariant K, @Covariant V> extends Map<K, V>
{
	interface Builder<K, V>
	{
		void put(K key, V value);

		default void put(@NonNull Entry<? extends K, ? extends V> entry)
		{
			this.put(entry.getKey(), entry.getValue());
		}

		default void putAll(@NonNull Map<? extends K, ? extends V> map)
		{
			for (Entry<? extends K, ? extends V> entry : map)
			{
				this.put(entry.getKey(), entry.getValue());
			}
		}

		ImmutableMap<K, V> build();
	}

	@NonNull
	static <K, V> ImmutableMap<K, V> apply()
	{
		return EmptyMap.apply();
	}

	@NonNull
	static <K, V> ImmutableMap<K, V> singleton(K key, V value)
	{
		return new SingletonMap<>(key, value);
	}

	@NonNull
	static <K, V> ImmutableMap<K, V> apply(@NonNull Entry<K, V> entry)
	{
		return SingletonMap.apply(entry.getKey(), entry.getValue());
	}

	@NonNull
	@SafeVarargs
	static <K, V> ImmutableMap<K, V> apply(@NonNull Entry<? extends K, ? extends V>... entries)
	{
		switch (entries.length)
		{
		case 0:
			return EmptyMap.apply();
		case 1:
			// Safe cast, Entry is covariant
			return SingletonMap.apply((Entry<K, V>) entries[0]);
		default:
			return TupleMap.apply(entries);
		}
	}

	@NonNull
	static <K, V> ImmutableMap<K, V> apply(K @NonNull [] keys, V[] values)
	{
		return new ArrayMap<>(keys, values, true);
	}

	@NonNull
	static <K, V> Builder<K, V> builder()
	{
		return new ArrayMap.Builder<>();
	}

	@NonNull
	static <K, V> Builder<K, V> builder(int capacity)
	{
		return new ArrayMap.Builder<>(capacity);
	}

	// Simple Getters

	@Override
	default boolean isImmutable()
	{
		return true;
	}

	@Override
	int size();

	@NonNull
	@Override
	Iterator<Entry<K, V>> iterator();

	@NonNull
	@Override
	default Spliterator<Entry<K, V>> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), Spliterator.IMMUTABLE);
	}

	@NonNull
	@Override
	Iterator<K> keyIterator();

	@NonNull
	@Override
	default Spliterator<K> keySpliterator()
	{
		return Spliterators.spliterator(this.keyIterator(), this.size(), Spliterator.IMMUTABLE);
	}

	@NonNull
	@Override
	Iterator<V> valueIterator();

	@NonNull
	@Override
	default Spliterator<V> valueSpliterator()
	{
		return Spliterators.spliterator(this.valueIterator(), this.size(), Spliterator.IMMUTABLE);
	}

	@Nullable
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
	ImmutableMap<K, V> withEntry(K key, V value);

	@NonNull
	@Override
	default ImmutableMap<K, V> withEntry(@NonNull Entry<? extends K, ? extends V> entry)
	{
		return this.withEntry(entry.getKey(), entry.getValue());
	}

	@NonNull
	@Override
	ImmutableMap<K, V> union(@NonNull Map<? extends K, ? extends V> map);

	@NonNull
	@Override
	ImmutableMap<K, V> keyRemoved(Object key);

	@NonNull
	@Override
	ImmutableMap<K, V> removed(Object key, Object value);

	@NonNull
	@Override
	default ImmutableMap<K, V> removed(@NonNull Entry<?, ?> entry)
	{
		return this.removed(entry.getKey(), entry.getValue());
	}

	@NonNull
	@Override
	ImmutableMap<K, V> valueRemoved(Object value);

	@NonNull
	@Override
	ImmutableMap<K, V> difference(@NonNull Map<?, ?> map);

	@NonNull
	@Override
	ImmutableMap<K, V> keyDifference(@NonNull Collection<?> keys);

	@Override
	default <NK> ImmutableMap<NK, V> keyMapped(@NonNull Function<? super K, ? extends NK> mapper)
	{
		return this.keyMapped((k, v) -> mapper.apply(k));
	}

	@Override
	<NK> ImmutableMap<NK, V> keyMapped(@NonNull BiFunction<? super K, ? super V, ? extends NK> mapper);

	@Nullable
	@Override
	default <NV> Map<K, NV> valueMapped(@NonNull Function<? super V, ? extends NV> mapper)
	{
		return this.valueMapped((k, v) -> mapper.apply(v));
	}

	@Nullable
	@Override
	<NV> ImmutableMap<K, NV> valueMapped(@NonNull BiFunction<? super K, ? super V, ? extends NV> mapper);

	@Override
	<NK, NV> ImmutableMap<NK, NV> entryMapped(@NonNull BiFunction<? super K, ? super V, ? extends Entry<? extends NK, ? extends NV>> mapper);

	@Nullable
	@Override
	<NK, NV> ImmutableMap<NK, NV> flatMapped(@NonNull BiFunction<? super K, ? super V, ? extends @NonNull Iterable<? extends Entry<? extends NK, ? extends NV>>> mapper);

	@NonNull
	@Override
	ImmutableMap<K, V> filtered(@NonNull BiPredicate<? super K, ? super V> condition);

	@NonNull
	@Override
	default ImmutableMap<K, V> filteredByKey(@NonNull Predicate<? super K> condition)
	{
		return this.filtered(((k, v) -> condition.test(k)));
	}

	@NonNull
	@Override
	default ImmutableMap<K, V> filteredByValue(@NonNull Predicate<? super V> condition)
	{
		return this.filtered(((k, v) -> condition.test(v)));
	}

	@NonNull
	@Override
	ImmutableMap<V, K> inverted();

	// Mutating Operations

	@Override
	@Mutating
	default void clear()
	{
		throw new ImmutableException("clear() on Immutable Map");
	}

	@Override
	@Mutating
	default void subscript_$eq(K key, V value)
	{
		throw new ImmutableException("() on Immutable Map");
	}

	@NonNull
	@Override
	@Mutating
	default V put(K key, V value)
	{
		throw new ImmutableException("put() on Immutable Map");
	}

	@NonNull
	@Override
	@Mutating
	default V put(@NonNull Entry<? extends K, ? extends V> entry)
	{
		throw new ImmutableException("put() on Immutable Map");
	}

	@Override
	@Mutating
	default void putAll(@NonNull Map<? extends K, ? extends V> map)
	{
		throw new ImmutableException("putAll() on Immutable Map");
	}

	@NonNull
	@Override
	@Mutating
	default V putIfAbsent(K key, V value)
	{
		throw new ImmutableException("putIfAbsent() on Immutable Map");
	}

	@NonNull
	@Override
	@Mutating
	default V putIfAbsent(@NonNull Entry<? extends K, ? extends V> entry)
	{
		throw new ImmutableException("putIfAbsent() on Immutable Map");
	}

	@Override
	@Mutating
	default boolean replace(K key, V oldValue, V newValue)
	{
		throw new ImmutableException("replace() on Immutable Map");
	}

	@NonNull
	@Override
	@Mutating
	default V replace(@NonNull Entry<? extends K, ? extends V> entry)
	{
		throw new ImmutableException("replace() on Immutable Map");
	}

	@NonNull
	@Override
	@Mutating
	default V replace(K key, V newValue)
	{
		throw new ImmutableException("replace() on Immutable Map");
	}

	@NonNull
	@Override
	@Mutating
	default V remap(Object key, K newKey)
	{
		throw new ImmutableException("remap() on Immutable Map");
	}

	@NonNull
	@Override
	@Mutating
	default V removeKey(Object key)
	{
		throw new ImmutableException("removeKey() on Immutable Map");
	}

	@Override
	@Mutating
	default boolean removeValue(Object value)
	{
		throw new ImmutableException("removeValue() on Immutable Map");
	}

	@Override
	@Mutating
	default boolean remove(Object key, Object value)
	{
		throw new ImmutableException("remove() on Immutable Map");
	}

	@Override
	@Mutating
	default boolean remove(@NonNull Entry<?, ?> entry)
	{
		throw new ImmutableException("remove() on Immutable Map");
	}

	@Override
	@Mutating
	default boolean removeKeys(@NonNull Collection<?> keys)
	{
		throw new ImmutableException("removeKeys() on Immutable Map");
	}

	@Override
	@Mutating
	default boolean removeAll(@NonNull Map<?, ?> map)
	{
		throw new ImmutableException("removeAll() on Immutable Map");
	}

	@Override
	@Mutating
	default void mapKeys(@NonNull Function<? super K, ? extends K> mapper)
	{
		throw new ImmutableException("mapKeys() on Immutable Map");
	}

	@Override
	@Mutating
	default void mapKeys(@NonNull BiFunction<? super K, ? super V, ? extends K> mapper)
	{
		throw new ImmutableException("mapKeys() on Immutable Map");
	}

	@Override
	@Mutating
	default void mapValues(@NonNull Function<? super V, ? extends V> mapper)
	{
		throw new ImmutableException("mapValues() on Immutable Map");
	}

	@Override
	@Mutating
	default void mapValues(@NonNull BiFunction<? super K, ? super V, ? extends V> mapper)
	{
		throw new ImmutableException("mapValues() on Immutable Map");
	}

	@Override
	@Mutating
	default void mapEntries(@NonNull BiFunction<? super K, ? super V, ? extends Entry<? extends K, ? extends V>> mapper)
	{
		throw new ImmutableException("mapEntries() on Immutable Map");
	}

	@Override
	@Mutating
	default void flatMap(@NonNull BiFunction<? super K, ? super V, ? extends @NonNull Iterable<? extends Entry<? extends K, ? extends V>>> mapper)
	{
		throw new ImmutableException("flatMap() on Immutable Map");
	}

	@Override
	@Mutating
	default void filter(@NonNull BiPredicate<? super K, ? super V> condition)
	{
		throw new ImmutableException("filter() on Immutable Map");
	}

	@Override
	@Mutating
	default void filterByKey(@NonNull Predicate<? super K> condition)
	{
		throw new ImmutableException("filterByKey() on Immutable Map");
	}

	@Override
	@Mutating
	default void filterByValue(@NonNull Predicate<? super V> condition)
	{
		throw new ImmutableException("filterByValue() on Immutable Map");
	}

	// Copying

	@NonNull
	@Override
	ImmutableMap<K, V> copy();

	@Override
	<RK, RV> MutableMap<RK, RV> emptyCopy();

	@Override
	default <RK, RV> MutableMap<RK, RV> emptyCopy(int capacity)
	{
		return this.emptyCopy();
	}

	@NonNull
	@Override
	MutableMap<K, V> mutable();

	@NonNull
	@Override
	default MutableMap<K, V> mutableCopy()
	{
		return this.mutable();
	}

	@Override
	<RK, RV> ImmutableMap.Builder<RK, RV> immutableBuilder();

	@Override
	default <RK, RV> Builder<RK, RV> immutableBuilder(int capacity)
	{
		return this.immutableBuilder();
	}

	@NonNull
	@Override
	default ImmutableMap<K, V> immutable()
	{
		return this;
	}

	@NonNull
	@Override
	default ImmutableMap<K, V> immutableCopy()
	{
		return this.copy();
	}

	@NonNull
	@Override
	default ImmutableMap<K, V> view()
	{
		return this;
	}

	@Override
	java.util.Map<K, V> toJava();
}
