package dyvil.collection.immutable;

import java.util.Collections;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import dyvil.lang.literal.ArrayConvertible;

import dyvil.collection.*;
import dyvil.collection.impl.AbstractHashMap;
import dyvil.math.MathUtils;
import dyvil.tuple.Tuple2;
import dyvil.util.ImmutableException;

@ArrayConvertible
public class HashMap<K, V> extends AbstractHashMap<K, V>implements ImmutableMap<K, V>
{
	public static <K, V> HashMap<K, V> apply(Tuple2<K, V>[] tuples)
	{
		return new HashMap<K, V>(tuples);
	}
	
	public static <K, V> Builder<K, V> builder()
	{
		return new Builder<K, V>();
	}
	
	public static <K, V> Builder<K, V> builder(int capacity)
	{
		return new Builder<K, V>(capacity);
	}
	
	public static class Builder<K, V> implements ImmutableMap.Builder<K, V>
	{
		private HashMap<K, V> map;
		
		public Builder()
		{
			this.map = new HashMap<K, V>();
		}
		
		public Builder(int capacity)
		{
			this.map = new HashMap<K, V>(capacity);
		}
		
		@Override
		public void put(K key, V value)
		{
			if (this.map == null)
			{
				throw new IllegalStateException("Already built!");
			}
			
			this.map.putInternal(key, value);
		}
		
		@Override
		public HashMap<K, V> build()
		{
			HashMap<K, V> map = this.map;
			this.map = null;
			map.flatten();
			return map;
		}
	}
	
	protected HashMap()
	{
		this(DEFAULT_CAPACITY);
	}
	
	protected HashMap(int capacity)
	{
		this.entries = new HashEntry[MathUtils.powerOfTwo(capacity)];
	}
	
	public HashMap(Map<K, V> map)
	{
		super(map);
	}
	
	public HashMap(AbstractHashMap<K, V> map)
	{
		super(map);
	}
	
	public HashMap(Tuple2<K, V>... tuples)
	{
		super(tuples);
	}
	
	@Override
	protected void addEntry(int hash, K key, V value, int index)
	{
		this.entries[index] = new HashEntry(key, value, hash, this.entries[index]);
		this.size++;
	}
	
	@Override
	protected void removeEntry(HashEntry<K, V> entry)
	{
		throw new ImmutableException("Iterator.remove() on Immutable Map");
	}
	
	@Override
	public ImmutableMap<K, V> $plus(K key, V value)
	{
		HashMap<K, V> newMap = new HashMap<K, V>(this);
		newMap.putInternal(key, value);
		newMap.flatten();
		return newMap;
	}
	
	@Override
	public ImmutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map)
	{
		HashMap<K, V> newMap = new HashMap<K, V>(this);
		newMap.putInternal(map);
		newMap.flatten();
		return newMap;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$at(Object key)
	{
		HashMap<K, V> newMap = new HashMap<K, V>(this.size);
		for (Entry<K, V> entry : this)
		{
			K entryKey = entry.getKey();
			if (!Objects.equals(entryKey, key))
			{
				newMap.putInternal(entryKey, entry.getValue());
			}
		}
		return newMap;
	}
	
	@Override
	public ImmutableMap<K, V> $minus(Object key, Object value)
	{
		HashMap<K, V> newMap = new HashMap<K, V>(this.size);
		for (Entry<K, V> entry : this)
		{
			K entryKey = entry.getKey();
			V entryValue = entry.getValue();
			if (!Objects.equals(entryKey, key) || !Objects.equals(entryValue, value))
			{
				newMap.putInternal(entryKey, entryValue);
			}
		}
		return newMap;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$colon(Object value)
	{
		HashMap<K, V> newMap = new HashMap<K, V>(this.size);
		for (Entry<K, V> entry : this)
		{
			V entryValue = entry.getValue();
			if (!Objects.equals(entryValue, value))
			{
				newMap.putInternal(entry.getKey(), entryValue);
			}
		}
		return newMap;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Map<?, ?> map)
	{
		HashMap<K, V> newMap = new HashMap<K, V>(this.size);
		for (Entry<K, V> entry : this)
		{
			K entryKey = entry.getKey();
			V entryValue = entry.getValue();
			if (!map.contains(entryKey, entryValue))
			{
				newMap.putInternal(entryKey, entryValue);
			}
		}
		return newMap;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Collection<?> keys)
	{
		HashMap<K, V> newMap = new HashMap<K, V>(this.size);
		for (Entry<K, V> entry : this)
		{
			K entryKey = entry.getKey();
			if (!keys.contains(entryKey))
			{
				newMap.putInternal(entryKey, entry.getValue());
			}
		}
		return newMap;
	}
	
	@Override
	public <U> ImmutableMap<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper)
	{
		HashMap<K, U> newMap = new HashMap<K, U>(this.size);
		for (Entry<K, V> entry : this)
		{
			K entryKey = entry.getKey();
			U entryValue = mapper.apply(entryKey, entry.getValue());
			newMap.putInternal(entryKey, entryValue);
		}
		return newMap;
	}
	
	@Override
	public <U, R> ImmutableMap<U, R> entryMapped(BiFunction<? super K, ? super V, ? extends Entry<? extends U, ? extends R>> mapper)
	{
		HashMap<U, R> newMap = new HashMap<U, R>(this.size);
		for (Entry<K, V> entry : this)
		{
			Entry<? extends U, ? extends R> newEntry = mapper.apply(entry.getKey(), entry.getValue());
			if (newEntry != null)
			{
				newMap.putInternal(newEntry.getKey(), newEntry.getValue());
			}
		}
		return newMap;
	}
	
	@Override
	public <U, R> ImmutableMap<U, R> flatMapped(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends U, ? extends R>>> mapper)
	{
		HashMap<U, R> newMap = new HashMap<U, R>(this.size);
		for (Entry<K, V> entry : this)
		{
			for (Entry<? extends U, ? extends R> newEntry : mapper.apply(entry.getKey(), entry.getValue()))
			{
				newMap.putInternal(newEntry.getKey(), newEntry.getValue());
			}
		}
		newMap.flatten();
		return newMap;
	}
	
	@Override
	public ImmutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		HashMap<K, V> newMap = new HashMap<K, V>(this.size);
		for (Entry<K, V> entry : this)
		{
			K entryKey = entry.getKey();
			V entryValue = entry.getValue();
			if (condition.test(entryKey, entryValue))
			{
				newMap.putInternal(entryKey, entryValue);
			}
		}
		return newMap;
	}
	
	@Override
	public ImmutableMap<V, K> inverted()
	{
		HashMap<V, K> newMap = new HashMap<V, K>(this.size);
		for (Entry<K, V> entry : this)
		{
			newMap.putInternal(entry.getValue(), entry.getKey());
		}
		return newMap;
	}
	
	@Override
	public ImmutableMap<K, V> copy()
	{
		return new HashMap<K, V>(this);
	}
	
	@Override
	public MutableMap<K, V> mutable()
	{
		return new dyvil.collection.mutable.HashMap<K, V>(this);
	}
	
	@Override
	public java.util.Map<K, V> toJava()
	{
		return Collections.unmodifiableMap(super.toJava());
	}
}
