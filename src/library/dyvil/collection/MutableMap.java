package dyvil.collection;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import dyvil.collection.mutable.HashMap;
import dyvil.collection.mutable.LinkedList;
import dyvil.collection.view.MapView;

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
	public default boolean isImmutable()
	{
		return false;
	}
	
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
	public default MutableMap<K, V> $minus$at(Object key)
	{
		MutableMap<K, V> copy = this.copy();
		copy.$minus$at$eq(key);
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
	public default MutableMap<K, V> $minus$minus(Map<?, ?> map)
	{
		MutableMap<K, V> copy = this.copy();
		copy.$minus$minus$eq(map);
		return copy;
	}
	
	@Override
	public default Map<K, V> $minus$minus(Collection<?> keys)
	{
		MutableMap<K, V> copy = this.copy();
		copy.$minus$minus$eq(keys);
		return copy;
	}
	
	@Override
	public default <U> MutableMap<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper)
	{
		MutableMap<K, U> copy = this.emptyCopy();
		for (Entry<K, V> entry : this)
		{
			K key = entry.getKey();
			copy.put(key, mapper.apply(key, entry.getValue()));
		}
		return copy;
	}
	
	@Override
	public default <U, R> Map<U, R> entryMapped(BiFunction<? super K, ? super V, ? extends Entry<? extends U, ? extends R>> mapper)
	{
		MutableMap<U, R> copy = this.emptyCopy();
		for (Entry<K, V> entry : this)
		{
			Entry<? extends U, ? extends R> newEntry = mapper.apply(entry.getKey(), entry.getValue());
			if (newEntry != null)
			{
				copy.put(newEntry);
			}
		}
		return copy;
	}
	
	@Override
	public default <U, R> Map<U, R> flatMapped(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends U, ? extends R>>> mapper)
	{
		MutableMap<U, R> copy = (MutableMap<U, R>) this.emptyCopy();
		for (Entry<K, V> entry : this)
		{
			for (Entry<? extends U, ? extends R> newEntry : mapper.apply(entry.getKey(), entry.getValue()))
			{
				copy.put(newEntry);
			}
		}
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
	public default void subscript_$eq(K key, V value)
	{
		this.put(key, value);
	}
	
	@Override
	public V put(K key, V value);
	
	@Override
	public default V put(Entry<? extends K, ? extends V> entry)
	{
		return this.put(entry.getKey(), entry.getValue());
	}
	
	@Override
	public default boolean putAll(Map<? extends K, ? extends V> map)
	{
		boolean added = false;
		for (Entry<? extends K, ? extends V> entry : map)
		{
			if (this.put(entry) == null)
			{
				added = true;
			}
		}
		return added;
	}
	
	@Override
	public V removeKey(Object key);
	
	@Override
	public boolean removeValue(Object value);
	
	@Override
	public boolean remove(Object key, Object value);
	
	@Override
	public default boolean remove(Entry<?, ?> entry)
	{
		return this.remove(entry.getKey(), entry.getValue());
	}
	
	@Override
	public default boolean removeKeys(Collection<?> keys)
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
	public default boolean removeAll(Map<?, ?> map)
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
	public void map(BiFunction<? super K, ? super V, ? extends V> mapper);
	
	@Override
	public default void mapEntries(BiFunction<? super K, ? super V, ? extends Entry<? extends K, ? extends V>> mapper)
	{
		List<Entry<? extends K, ? extends V>> entryList = new LinkedList();
		for (Entry<K, V> entry : this)
		{
			Entry<? extends K, ? extends V> newEntry = mapper.apply(entry.getKey(), entry.getValue());
			if (newEntry != null)
			{
				entryList.add(entry);
			}
		}
		
		this.clear();
		for (Entry<? extends K, ? extends V> entry : entryList)
		{
			this.put(entry);
		}
	}
	
	@Override
	public default void flatMap(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends K, ? extends V>>> mapper)
	{
		List<Entry<? extends K, ? extends V>> entryList = new LinkedList();
		for (Entry<K, V> entry : this)
		{
			for (Entry<? extends K, ? extends V> newEntry : mapper.apply(entry.getKey(), entry.getValue()))
			{
				entryList.add(newEntry);
			}
		}
		
		this.clear();
		for (Entry<? extends K, ? extends V> entry : entryList)
		{
			this.put(entry);
		}
	}
	
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
	
	@Override
	public default ImmutableMap<K, V> view()
	{
		return new MapView(this);
	}
}
