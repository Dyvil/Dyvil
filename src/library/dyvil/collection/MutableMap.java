package dyvil.collection;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import dyvil.collection.mutable.HashMap;
import dyvil.lang.Entry;
import dyvil.lang.Map;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

@NilConvertible
@ArrayConvertible
public interface MutableMap<K, V> extends Map<K, V>
{
	public static <K, V> MutableMap<K, V> apply()
	{
		return new HashMap();
	}
	
	public static <K, V> MutableMap<K, V> apply(K key, V value)
	{
		HashMap<K, V> map = new HashMap(1);
		map.update(key, value);
		return map;
	}
	
	public static <K, V> MutableMap<K, V> apply(Entry<K, V> entry)
	{
		return apply(entry.getKey(), entry.getValue());
	}
	
	public static <K, V> MutableMap<K, V> apply(Entry<? extends K, ? extends V>... entries)
	{
		HashMap<K, V> map = new HashMap(entries.length);
		for (Entry<? extends K, ? extends V> entry : entries)
		{
			map.update(entry.getKey(), entry.getValue());
		}
		return map;
	}
	
	// Simple Getters
	
	@Override
	public int size();
	
	@Override
	public boolean isEmpty();
	
	@Override
	public Iterator<Entry<K, V>> iterator();
	
	@Override
	public default Spliterator<Entry<K, V>> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), 0);
	}
	
	@Override
	public Iterator<K> keyIterator();
	
	@Override
	public Iterator<V> valueIterator();
	
	@Override
	public void forEach(Consumer<? super Entry<K, V>> action);
	
	@Override
	public void forEach(BiConsumer<? super K, ? super V> action);
	
	@Override
	public boolean $qmark(Object key);
	
	@Override
	public boolean $qmark(Object key, Object value);
	
	@Override
	public default boolean $qmark(Entry<? extends K, ? extends V> entry)
	{
		return this.$qmark(entry.getKey(), entry.getValue());
	}
	
	@Override
	public boolean $qmark$colon(V value);
	
	@Override
	public V apply(K key);
	
	// Non-mutating Operations
	
	@Override
	public MutableMap<K, V> $plus(K key, V value);
	
	@Override
	public default MutableMap<K, V> $plus(Entry<? extends K, ? extends V> entry)
	{
		return this.$plus(entry.getKey(), entry.getValue());
	}
	
	@Override
	public MutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map);
	
	@Override
	public MutableMap<K, V> $minus(K key);
	
	@Override
	public MutableMap<K, V> $minus(K key, V value);
	
	@Override
	public default MutableMap<K, V> $minus(Entry<? extends K, ? extends V> entry)
	{
		return this.$minus(entry.getKey(), entry.getValue());
	}
	
	@Override
	public MutableMap<K, V> $minus$colon(V value);
	
	@Override
	public MutableMap<K, V> $minus$minus(Map<? extends K, ? extends V> map);
	
	@Override
	public <U> MutableMap<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper);
	
	@Override
	public MutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition);
	
	// Mutating Operations
	
	@Override
	public void clear();
	
	@Override
	public void update(K key, V value);
	
	@Override
	public V put(K key, V value);
	
	@Override
	public default void $plus$eq(Entry<? extends K, ? extends V> entry)
	{
		this.update(entry.getKey(), entry.getValue());
	}
	
	@Override
	public void $plus$plus$eq(Map<? extends K, ? extends V> map);
	
	@Override
	public void $minus$eq(K key);
	
	@Override
	public V remove(K key);
	
	@Override
	public boolean remove(K key, V value);
	
	@Override
	public default void $minus$eq(Entry<? extends K, ? extends V> entry)
	{
		this.remove(entry.getKey(), entry.getValue());
	}
	
	@Override
	public void $minus$colon$eq(V value);
	
	@Override
	public void $minus$minus$eq(Map<? extends K, ? extends V> map);
	
	@Override
	public void map(BiFunction<? super K, ? super V, ? extends V> mapper);
	
	@Override
	public void filter(BiPredicate<? super K, ? super V> condition);
	
	// Copying
	
	@Override
	public MutableMap<K, V> copy();
	
	@Override
	public default MutableMap<K, V> mutable()
	{
		return this;
	}
	
	@Override
	public ImmutableMap<K, V> immutable();
}
