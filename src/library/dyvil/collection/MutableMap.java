package dyvil.collection;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import dyvil.collection.mutable.HashMap;

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
		map.subscript_$eq(key, value);
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
			map.subscript_$eq(entry.getKey(), entry.getValue());
		}
		return map;
	}
	
	// Simple Getters
	
	@Override
	public int size();
	
	@Override
	public Iterator<Entry<K, V>> iterator();
	
	@Override
	public Iterator<K> keyIterator();
	
	@Override
	public Iterator<V> valueIterator();
	
	@Override
	public boolean containsKey(Object key);
	
	@Override
	public boolean containsValue(Object value);
	
	@Override
	public V get(Object key);
	
	// Non-mutating Operations
	
	@Override
	public default MutableMap<K, V> $plus(K key, V value)
	{
		MutableMap<K, V> copy = this.copy();
		copy.subscript_$eq(key, value);
		return copy;
	}
	
	@Override
	public default MutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map)
	{
		MutableMap<K, V> copy = this.copy();
		copy.$plus$plus$eq(map);
		return copy;
	}
	
	@Override
	public default MutableMap<K, V> $minus(Object key)
	{
		MutableMap<K, V> copy = this.copy();
		copy.$minus$eq(key);
		return copy;
	}
	
	@Override
	public default MutableMap<K, V> $minus(Object key, Object value)
	{
		MutableMap<K, V> copy = this.copy();
		copy.$minus$eq(key, value);
		return copy;
	}
	
	@Override
	public default MutableMap<K, V> $minus$colon(Object value)
	{
		MutableMap<K, V> copy = this.copy();
		copy.$minus$colon$eq(value);
		return copy;
	}
	
	@Override
	public default MutableMap<K, V> $minus$minus(Map<? super K, ? super V> map)
	{
		MutableMap<K, V> copy = this.copy();
		copy.$minus$minus$eq(map);
		return copy;
	}
	
	@Override
	public default <U> MutableMap<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper)
	{
		MutableMap<K, U> copy = (MutableMap<K, U>) this.copy();
		copy.map((BiFunction) mapper);
		return copy;
	}
	
	@Override
	public default MutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		MutableMap<K, V> copy = this.copy();
		copy.filter(condition);
		return copy;
	}
	
	// Mutating Operations
	
	@Override
	public void clear();
	
	@Override
	public V put(K key, V value);
	
	@Override
	public V removeKey(Object key);
	
	@Override
	public boolean removeValue(Object value);
	
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
	public default MutableMap<K, V> mutableCopy()
	{
		return this.copy();
	}
	
	public <RK, RV> MutableMap<RK, RV> emptyCopy();
	
	@Override
	public ImmutableMap<K, V> immutable();
	
	@Override
	public default ImmutableMap<K, V> immutableCopy()
	{
		return this.immutable();
	}
}
