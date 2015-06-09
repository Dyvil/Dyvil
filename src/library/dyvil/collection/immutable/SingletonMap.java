package dyvil.collection.immutable;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import dyvil.collection.ImmutableMap;
import dyvil.collection.MutableMap;
import dyvil.collection.iterator.SingletonIterator;
import dyvil.lang.Entry;
import dyvil.lang.Map;
import dyvil.tuple.Tuple2;

public class SingletonMap<K, V> implements ImmutableMap<K, V>, Entry<K, V>
{
	private K	key;
	private V	value;
	
	public static <K, V> SingletonMap<K, V> apply(K key, V value)
	{
		return new SingletonMap(key, value);
	}
	
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
	public Iterator<Entry<K, V>> iterator()
	{
		return new SingletonIterator<>(this);
	}
	
	@Override
	public Iterator<K> keyIterator()
	{
		return new SingletonIterator(this.key);
	}
	
	@Override
	public Iterator<V> valueIterator()
	{
		return new SingletonIterator(this.value);
	}
	
	@Override
	public K getKey()
	{
		return this.key;
	}
	
	@Override
	public V getValue()
	{
		return this.value;
	}
	
	@Override
	public void forEach(Consumer<? super Entry<K, V>> action)
	{
		action.accept(this);
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
		return new ArrayMap(new Object[] { this.key, key }, new Object[] { this.value, value }, 2, true);
	}
	
	@Override
	public ImmutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map)
	{
		int index = 1;
		Tuple2<? extends K, ? extends V>[] tuples = new Tuple2[1 + map.size()];
		tuples[0] = new Tuple2(this.key, this.value);
		for (Entry<? extends K, ? extends V> entry : map)
		{
			tuples[index++] = new Tuple2<K, V>(entry.getKey(), entry.getValue());
		}
		return new TupleMap(tuples, index);
	}
	
	@Override
	public ImmutableMap<K, V> $minus(Object key)
	{
		return Objects.equals(this.key, key) ? EmptyMap.emptyMap : this;
	}
	
	@Override
	public ImmutableMap<K, V> $minus(Object key, Object value)
	{
		return Objects.equals(this.key, key) && Objects.equals(this.value, value) ? EmptyMap.emptyMap : this;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$colon(Object value)
	{
		return Objects.equals(this.value, value) ? EmptyMap.emptyMap : this;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Map<? super K, ? super V> map)
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
	
	@Override
	public boolean equals(Object obj)
	{
		return Map.mapEquals(this, obj);
	}
	
	@Override
	public int hashCode()
	{
		return Map.mapHashCode(this);
	}
}
