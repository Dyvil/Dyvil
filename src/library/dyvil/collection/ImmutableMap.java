package dyvil.collection;

import dyvil.annotation.Mutating;
import dyvil.annotation._internal.Covariant;
import dyvil.collection.immutable.ArrayMap;
import dyvil.collection.immutable.EmptyMap;
import dyvil.collection.immutable.SingletonMap;
import dyvil.collection.immutable.TupleMap;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.MapConvertible;
import dyvil.lang.literal.NilConvertible;
import dyvil.util.ImmutableException;
import dyvil.util.Option;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

@NilConvertible
@ArrayConvertible
@MapConvertible
public interface ImmutableMap<@Covariant K, @Covariant V> extends Map<K, V>
{
	interface Builder<K, V>
	{
		void put(K key, V value);
		
		default void put(Entry<? extends K, ? extends V> entry)
		{
			this.put(entry.getKey(), entry.getValue());
		}
		
		default void putAll(Map<? extends K, ? extends V> map)
		{
			for (Entry<? extends K, ? extends V> entry : map)
			{
				this.put(entry.getKey(), entry.getValue());
			}
		}
		
		ImmutableMap<K, V> build();
	}
	
	static <K, V> ImmutableMap<K, V> apply()
	{
		return EmptyMap.apply();
	}
	
	static <K, V> ImmutableMap<K, V> apply(Entry<K, V> entry)
	{
		return new SingletonMap<>(entry.getKey(), entry.getValue());
	}
	
	@SafeVarargs
	static <K, V> ImmutableMap<K, V> apply(Entry<? extends K, ? extends V>... entries)
	{
		switch (entries.length)
		{
		case 0:
			return EmptyMap.apply();
		case 1:
			Entry<? extends K, ? extends V> entry = entries[0];
			return new SingletonMap<>(entry.getKey(), entry.getValue());
		default:
			// Save cast, Entry is covariant
			return new TupleMap<>((Entry<K, V>[]) entries);
		}
	}
	
	static <K, V> ImmutableMap<K, V> apply(K[] keys, V[] values)
	{
		return new ArrayMap<>(keys, values, true);
	}
	
	static <K, V> Builder<K, V> builder()
	{
		return new ArrayMap.Builder<>();
	}
	
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
	
	@Override
	Iterator<Entry<K, V>> iterator();
	
	@Override
	default Spliterator<Entry<K, V>> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), Spliterator.IMMUTABLE);
	}
	
	@Override
	Iterator<K> keyIterator();
	
	@Override
	default Spliterator<K> keySpliterator()
	{
		return Spliterators.spliterator(this.keyIterator(), this.size(), Spliterator.IMMUTABLE);
	}
	
	@Override
	Iterator<V> valueIterator();
	
	@Override
	default Spliterator<V> valueSpliterator()
	{
		return Spliterators.spliterator(this.valueIterator(), this.size(), Spliterator.IMMUTABLE);
	}
	
	@Override
	V get(Object key);
	
	@Override
	Option<V> getOption(Object key);
	
	// Non-mutating Operations
	
	@Override
	ImmutableMap<K, V> $plus(K key, V value);
	
	@Override
	default ImmutableMap<K, V> $plus(Entry<? extends K, ? extends V> entry)
	{
		return this.$plus(entry.getKey(), entry.getValue());
	}
	
	@Override
	ImmutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map);
	
	@Override
	ImmutableMap<K, V> $minus$at(Object key);
	
	@Override
	ImmutableMap<K, V> $minus(Object key, Object value);
	
	@Override
	default ImmutableMap<K, V> $minus(Entry<?, ?> entry)
	{
		return this.$minus(entry.getKey(), entry.getValue());
	}
	
	@Override
	ImmutableMap<K, V> $minus$colon(Object value);
	
	@Override
	ImmutableMap<K, V> $minus$minus(Map<?, ?> map);
	
	@Override
	ImmutableMap<K, V> $minus$minus(Collection<?> keys);
	
	@Override
	<NK> ImmutableMap<NK, V> keyMapped(BiFunction<? super K, ? super V, ? extends NK> mapper);
	
	@Override
	<NV> ImmutableMap<K, NV> valueMapped(BiFunction<? super K, ? super V, ? extends NV> mapper);
	
	@Override
	<NK, NV> ImmutableMap<NK, NV> entryMapped(BiFunction<? super K, ? super V, ? extends Entry<? extends NK, ? extends NV>> mapper);
	
	@Override
	<NK, NV> ImmutableMap<NK, NV> flatMapped(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends NK, ? extends NV>>> mapper);
	
	@Override
	ImmutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition);
	
	@Override
	ImmutableMap<V, K> inverted();
	
	// Mutating Operations
	
	@Override
	@Mutating
	default void $plus$eq(Entry<? extends K, ? extends V> entry)
	{
		throw new ImmutableException("+= on Immutable Map");
	}
	
	@Override
	@Mutating
	default void $plus$plus$eq(Map<? extends K, ? extends V> map)
	{
		throw new ImmutableException("+= on Immutable Map");
	}
	
	@Override
	@Mutating
	default void $minus$at$eq(Object key)
	{
		throw new ImmutableException("-= on Immutable Map");
	}
	
	@Override
	@Mutating
	default void $minus$eq(Entry<?, ?> entry)
	{
		throw new ImmutableException("-= on Immutable Map");
	}
	
	@Override
	@Mutating
	default void $minus$colon$eq(Object value)
	{
		throw new ImmutableException("-:= on Immutable Map");
	}
	
	@Override
	@Mutating
	default void $minus$minus$eq(Map<?, ?> map)
	{
		throw new ImmutableException("-= on Immutable Map");
	}
	
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
	
	@Override
	@Mutating
	default V put(K key, V value)
	{
		throw new ImmutableException("put() on Immutable Map");
	}
	
	@Override
	@Mutating
	default V put(Entry<? extends K, ? extends V> entry)
	{
		throw new ImmutableException("put() on Immutable Map");
	}
	
	@Override
	@Mutating
	default void putAll(Map<? extends K, ? extends V> map)
	{
		throw new ImmutableException("putAll() on Immutable Map");
	}
	
	@Override
	@Mutating
	default V putIfAbsent(K key, V value)
	{
		throw new ImmutableException("putIfAbsent() on Immutable Map");
	}
	
	@Override
	@Mutating
	default V putIfAbsent(Entry<? extends K, ? extends V> entry)
	{
		throw new ImmutableException("putIfAbsent() on Immutable Map");
	}
	
	@Override
	@Mutating
	default boolean replace(K key, V oldValue, V newValue)
	{
		throw new ImmutableException("replace() on Immutable Map");
	}
	
	@Override
	@Mutating
	default V replace(Entry<? extends K, ? extends V> entry)
	{
		throw new ImmutableException("replace() on Immutable Map");
	}
	
	@Override
	@Mutating
	default V replace(K key, V newValue)
	{
		throw new ImmutableException("replace() on Immutable Map");
	}
	
	@Override
	@Mutating
	default V remap(Object key, K newKey)
	{
		throw new ImmutableException("remap() on Immutable Map");
	}

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
	default boolean remove(Entry<?, ?> entry)
	{
		throw new ImmutableException("remove() on Immutable Map");
	}
	
	@Override
	@Mutating
	default boolean removeKeys(Collection<?> keys)
	{
		throw new ImmutableException("removeKeys() on Immutable Map");
	}
	
	@Override
	@Mutating
	default boolean removeAll(Map<?, ?> map)
	{
		throw new ImmutableException("removeAll() on Immutable Map");
	}
	
	@Override
	@Mutating
	default void mapKeys(BiFunction<? super K, ? super V, ? extends K> mapper)
	{
		throw new ImmutableException("mapKeys() on Immutable Map");
	}
	
	@Override
	@Mutating
	default void mapValues(BiFunction<? super K, ? super V, ? extends V> mapper)
	{
		throw new ImmutableException("mapValues() on Immutable Map");
	}
	
	@Override
	@Mutating
	default void mapEntries(BiFunction<? super K, ? super V, ? extends Entry<? extends K, ? extends V>> mapper)
	{
		throw new ImmutableException("mapEntries() on Immutable Map");
	}
	
	@Override
	@Mutating
	default void flatMap(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends K, ? extends V>>> mapper)
	{
		throw new ImmutableException("flatMap() on Immutable Map");
	}
	
	@Override
	@Mutating
	default void filter(BiPredicate<? super K, ? super V> condition)
	{
		throw new ImmutableException("filter() on Immutable Map");
	}
	
	// Copying
	
	@Override
	ImmutableMap<K, V> copy();

	@Override
	<RK, RV> MutableMap<RK, RV> emptyCopy();

	@Override
	default <RK, RV> MutableMap<RK, RV> emptyCopy(int capacity)
	{
		return this.emptyCopy();
	}

	@Override
	MutableMap<K, V> mutable();
	
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

	@Override
	default ImmutableMap<K, V> immutable()
	{
		return this;
	}
	
	@Override
	default ImmutableMap<K, V> immutableCopy()
	{
		return this.copy();
	}
	
	@Override
	default ImmutableMap<K, V> view()
	{
		return this;
	}
	
	@Override
	java.util.Map<K, V> toJava();
}
