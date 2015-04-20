package dyvil.collection.immutable;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import dyvil.collection.SingletonIterator;
import dyvil.collection.mutable.MutableMap;
import dyvil.lang.Map;
import dyvil.lang.tuple.Tuple2;

public class SingletonMap<K, V> implements ImmutableMap<K, V>
{
	private K	key;
	private V	value;
	
	public SingletonMap(K key, V value)
	{
		this.key = key;
		this.value = value;
	}
	
	@Override
	public int size()
	{
		return 1;
	}
	
	@Override
	public boolean isEmpty()
	{
		return false;
	}
	
	@Override
	public Iterator<Tuple2<K, V>> iterator()
	{
		return new SingletonIterator<>(new Tuple2<>(this.key, this.value));
	}
	
	@Override
	public void forEach(Consumer<? super Tuple2<K, V>> action)
	{
		action.accept(new Tuple2<>(this.key, this.value));
	}
	
	@Override
	public void forEach(BiConsumer<? super K, ? super V> action)
	{
		action.accept(this.key, this.value);
	}
	
	@Override
	public boolean $qmark(Object key)
	{
		return Objects.equals(this.key, key);
	}
	
	@Override
	public boolean $qmark(Object key, Object value)
	{
		return Objects.equals(this.key, key) && Objects.equals(this.value, value);
	}
	
	@Override
	public boolean $qmark$colon(V value)
	{
		return Objects.equals(this.value, value);
	}
	
	@Override
	public V apply(K key)
	{
		return Objects.equals(this.key, key) ? this.value : null;
	}
	
	@Override
	public ImmutableMap<K, V> $plus(K key, V value)
	{
		return ImmutableMap.apply(this.key, this.value, key, value);
	}
	
	@Override
	public ImmutableMap<K, V> $plus(Map<? extends K, ? extends V> map)
	{
		// TODO This is not the optimal solution.
		return ((ImmutableMap<K, V>) map.immutable()).$plus(this.key, this.value);
	}
	
	@Override
	public ImmutableMap<K, V> $minus(K key)
	{
		return Objects.equals(this.key, key) ? EmptyMap.emptyMap : this;
	}
	
	@Override
	public ImmutableMap<K, V> $minus(K key, V value)
	{
		return Objects.equals(this.key, key) && Objects.equals(this.value, value) ? EmptyMap.emptyMap : this;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$colon(V value)
	{
		return Objects.equals(this.value, value) ? EmptyMap.emptyMap : this;
	}
	
	@Override
	public ImmutableMap<K, V> $minus(Map<? extends K, ? extends V> map)
	{
		return map.$qmark(this.key, this.value) ? EmptyMap.emptyMap : this;
	}
	
	@Override
	public <U> ImmutableMap<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper)
	{
		return new SingletonMap(this.key, mapper.apply(this.key, this.value));
	}
	
	@Override
	public ImmutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		return condition.test(this.key, this.value) ? this : EmptyMap.emptyMap;
	}
	
	@Override
	public ImmutableMap<K, V> copy()
	{
		return new SingletonMap(this.key, this.value);
	}
	
	@Override
	public MutableMap<K, V> mutable()
	{
		return MutableMap.apply(this.key, this.value);
	}
	
	@Override
	public String toString()
	{
		return "[ " + this.key + " -> " + this.value + " ]";
	}
}
