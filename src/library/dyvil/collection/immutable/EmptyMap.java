package dyvil.collection.immutable;

import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import dyvil.collection.EmptyIterator;
import dyvil.collection.mutable.MutableMap;
import dyvil.lang.Map;
import dyvil.lang.tuple.Tuple2;

public class EmptyMap<K, V> implements ImmutableMap<K, V>
{
	static final EmptyMap	emptyMap	= new EmptyMap();
	
	public EmptyMap()
	{
	}
	
	@Override
	public int size()
	{
		return 0;
	}
	
	@Override
	public boolean isEmpty()
	{
		return true;
	}
	
	@Override
	public Iterator<Tuple2<K, V>> iterator()
	{
		return new EmptyIterator();
	}
	
	@Override
	public void forEach(Consumer<? super Tuple2<K, V>> action)
	{
	}
	
	@Override
	public void forEach(BiConsumer<? super K, ? super V> action)
	{
	}
	
	@Override
	public boolean $qmark(Object key)
	{
		return false;
	}
	
	@Override
	public boolean $qmark(Object key, Object value)
	{
		return false;
	}
	
	@Override
	public boolean $qmark$colon(V value)
	{
		return false;
	}
	
	@Override
	public V apply(K key)
	{
		return null;
	}
	
	@Override
	public ImmutableMap<K, V> $plus(K key, V value)
	{
		return ImmutableMap.apply(key, value);
	}
	
	@Override
	public ImmutableMap<K, V> $plus(Map<? extends K, ? extends V> map)
	{
		return (ImmutableMap<K, V>) map.immutable();
	}
	
	@Override
	public ImmutableMap<K, V> $minus(K key)
	{
		return this;
	}
	
	@Override
	public ImmutableMap<K, V> $minus(K key, V value)
	{
		return this;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$colon(V value)
	{
		return this;
	}
	
	@Override
	public ImmutableMap<K, V> $minus(Map<? extends K, ? extends V> map)
	{
		return this;
	}
	
	@Override
	public <U> ImmutableMap<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper)
	{
		return (ImmutableMap<K, U>) this;
	}
	
	@Override
	public ImmutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		return this;
	}
	
	@Override
	public ImmutableMap<K, V> copy()
	{
		return this;
	}
	
	@Override
	public MutableMap<K, V> mutable()
	{
		return MutableMap.apply();
	}
	
	@Override
	public String toString()
	{
		return "[]";
	}
}
