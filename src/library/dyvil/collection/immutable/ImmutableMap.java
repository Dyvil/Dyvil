package dyvil.collection.immutable;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import dyvil.collection.mutable.MutableMap;
import dyvil.lang.Immutable;
import dyvil.lang.ImmutableException;
import dyvil.lang.Map;
import dyvil.lang.tuple.Tuple2;

public interface ImmutableMap<K, V> extends Map<K, V>, Immutable
{
	public static <K, V> ImmutableMap<K, V> apply()
	{
		return EmptyMap.emptyMap;
	}
	
	public static <K, V> ImmutableMap<K, V> apply(K key, V value)
	{
		return new SingletonMap(key, value);
	}
	
	public static <K, V> ImmutableMap<K, V> apply(Tuple2<K, V> entry)
	{
		return new SingletonMap(entry._1, entry._2);
	}
	
	public static <K, V> ImmutableMap<K, V> apply(K key1, V value1, K key2, V value2)
	{
		return null; // FIXME
	}
	
	public static <K, V> ImmutableMap<K, V> apply(Tuple2<? extends K, ? extends V>[] entries)
	{
		switch (entries.length)
		{
		case 0:
			return EmptyMap.emptyMap;
		case 1:
			Tuple2<? extends K, ? extends V> entry = entries[0];
			return new SingletonMap(entry._1, entry._2);
		}
		return null; // FIXME
	}
	
	// Simple Getters
	
	@Override
	public int size();
	
	@Override
	public boolean isEmpty();
	
	@Override
	public Iterator<Tuple2<K, V>> iterator();
	
	@Override
	public default Spliterator<Tuple2<K, V>> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), 0);
	}
	
	@Override
	public Iterator<K> keyIterator();
	
	@Override
	public Iterator<V> valueIterator();
	
	@Override
	public void forEach(Consumer<? super Tuple2<K, V>> action);
	
	@Override
	public void forEach(BiConsumer<? super K, ? super V> action);
	
	@Override
	public boolean $qmark(Object key);
	
	@Override
	public boolean $qmark(Object key, Object value);
	
	@Override
	public default boolean $qmark(Tuple2<? extends K, ? extends V> entry)
	{
		return this.$qmark(entry._1, entry._2);
	}
	
	@Override
	public boolean $qmark$colon(V value);
	
	@Override
	public V apply(K key);
	
	// Non-mutating Operations
	
	@Override
	public ImmutableMap<K, V> $plus(K key, V value);
	
	@Override
	public default ImmutableMap<K, V> $plus(Tuple2<? extends K, ? extends V> entry)
	{
		return this.$plus(entry._1, entry._2);
	}
	
	@Override
	public ImmutableMap<K, V> $plus(Map<? extends K, ? extends V> map);
	
	@Override
	public ImmutableMap<K, V> $minus(K key);
	
	@Override
	public ImmutableMap<K, V> $minus(K key, V value);
	
	@Override
	public default ImmutableMap<K, V> $minus(Tuple2<? extends K, ? extends V> entry)
	{
		return this.$minus(entry._1, entry._2);
	}
	
	@Override
	public ImmutableMap<K, V> $minus$colon(V value);
	
	@Override
	public ImmutableMap<K, V> $minus(Map<? extends K, ? extends V> map);
	
	@Override
	public <U> ImmutableMap<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper);
	
	@Override
	public ImmutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition);
	
	// Mutating Operations
	
	@Override
	public default void clear()
	{
		throw new ImmutableException("clear() on Immutable Map");
	}
	
	@Override
	public default void update(K key, V value)
	{
		throw new ImmutableException("() on Immutable Map");
	}
	
	@Override
	public default void $plus$eq(K key, V value)
	{
		throw new ImmutableException("+= on Immutable Map");
	}
	
	@Override
	public default void $plus$eq(Tuple2<? extends K, ? extends V> entry)
	{
		throw new ImmutableException("+= on Immutable Map");
	}
	
	@Override
	public default void $plus$eq(Map<? extends K, ? extends V> map)
	{
		throw new ImmutableException("+= on Immutable Map");
	}
	
	@Override
	public default void $minus$eq(K key)
	{
		throw new ImmutableException("-= on Immutable Map");
	}
	
	@Override
	public default void $minus$eq(K key, V value)
	{
		throw new ImmutableException("-= on Immutable Map");
	}
	
	@Override
	public default void $minus$eq(Tuple2<? extends K, ? extends V> entry)
	{
		throw new ImmutableException("-= on Immutable Map");
	}
	
	@Override
	public default void $minus$colon$eq(V value)
	{
		throw new ImmutableException("-:= on Immutable Map");
	}
	
	@Override
	public default void $minus$eq(Map<? extends K, ? extends V> map)
	{
		throw new ImmutableException("-= on Immutable Map");
	}
	
	@Override
	public default void map(BiFunction<? super K, ? super V, ? extends V> mapper)
	{
		throw new ImmutableException("map() on Immutable Map");
	}
	
	@Override
	public default void filter(BiPredicate<? super K, ? super V> condition)
	{
		throw new ImmutableException("filter() on Immutable Map");
	}
	
	// Copying
	
	@Override
	public ImmutableMap<K, V> copy();
	
	@Override
	public MutableMap<K, V> mutable();
	
	@Override
	public default ImmutableMap<K, V> immutable()
	{
		return this;
	}
}
