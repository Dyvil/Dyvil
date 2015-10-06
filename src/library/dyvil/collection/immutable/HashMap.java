package dyvil.collection.immutable;

import java.util.Collections;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import dyvil.lang.literal.ArrayConvertible;

import dyvil.collection.*;
import dyvil.collection.impl.AbstractHashMap;
import dyvil.tuple.Tuple2;
import dyvil.util.ImmutableException;

@ArrayConvertible
public class HashMap<K, V> extends AbstractHashMap<K, V>implements ImmutableMap<K, V>
{
	public static <K, V> HashMap<K, V> apply(Tuple2<K, V>... tuples)
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
		super(DEFAULT_CAPACITY);
	}
	
	protected HashMap(int capacity)
	{
		super(capacity);
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
		HashMap<K, V> copy = new HashMap<K, V>(this);
		copy.ensureCapacity(this.size + 1);
		copy.putInternal(key, value);
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map)
	{
		HashMap<K, V> copy = new HashMap<K, V>(this);
		copy.putInternal(map);
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$at(Object key)
	{
		HashMap<K, V> copy = new HashMap<K, V>(this.size);
		for (Entry<K, V> entry : this)
		{
			K entryKey = entry.getKey();
			if (!Objects.equals(entryKey, key))
			{
				copy.putInternal(entryKey, entry.getValue());
			}
		}
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> $minus(Object key, Object value)
	{
		HashMap<K, V> copy = new HashMap<K, V>(this.size);
		for (Entry<K, V> entry : this)
		{
			K entryKey = entry.getKey();
			V entryValue = entry.getValue();
			if (!Objects.equals(entryKey, key) || !Objects.equals(entryValue, value))
			{
				copy.putInternal(entryKey, entryValue);
			}
		}
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$colon(Object value)
	{
		HashMap<K, V> copy = new HashMap<K, V>(this.size);
		for (Entry<K, V> entry : this)
		{
			V entryValue = entry.getValue();
			if (!Objects.equals(entryValue, value))
			{
				copy.putInternal(entry.getKey(), entryValue);
			}
		}
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Map<?, ?> map)
	{
		HashMap<K, V> copy = new HashMap<K, V>(this.size);
		for (Entry<K, V> entry : this)
		{
			K entryKey = entry.getKey();
			V entryValue = entry.getValue();
			if (!map.contains(entryKey, entryValue))
			{
				copy.putInternal(entryKey, entryValue);
			}
		}
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Collection<?> keys)
	{
		HashMap<K, V> copy = new HashMap<K, V>(this.size);
		for (Entry<K, V> entry : this)
		{
			K entryKey = entry.getKey();
			if (!keys.contains(entryKey))
			{
				copy.putInternal(entryKey, entry.getValue());
			}
		}
		return copy;
	}
	
	@Override
	public <U> ImmutableMap<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper)
	{
		HashMap<K, U> copy = new HashMap<K, U>(this.size);
		for (Entry<K, V> entry : this)
		{
			K entryKey = entry.getKey();
			U entryValue = mapper.apply(entryKey, entry.getValue());
			copy.putInternal(entryKey, entryValue);
		}
		return copy;
	}
	
	@Override
	public <U, R> ImmutableMap<U, R> entryMapped(BiFunction<? super K, ? super V, ? extends Entry<? extends U, ? extends R>> mapper)
	{
		HashMap<U, R> copy = new HashMap<U, R>(this.size);
		for (Entry<K, V> entry : this)
		{
			Entry<? extends U, ? extends R> newEntry = mapper.apply(entry.getKey(), entry.getValue());
			if (newEntry != null)
			{
				copy.putInternal(newEntry.getKey(), newEntry.getValue());
			}
		}
		return copy;
	}
	
	@Override
	public <U, R> ImmutableMap<U, R> flatMapped(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends U, ? extends R>>> mapper)
	{
		HashMap<U, R> copy = new HashMap<U, R>(this.size);
		for (Entry<K, V> entry : this)
		{
			for (Entry<? extends U, ? extends R> newEntry : mapper.apply(entry.getKey(), entry.getValue()))
			{
				copy.putInternal(newEntry.getKey(), newEntry.getValue());
			}
		}
		copy.flatten();
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		HashMap<K, V> copy = new HashMap<K, V>(this.size);
		for (Entry<K, V> entry : this)
		{
			K entryKey = entry.getKey();
			V entryValue = entry.getValue();
			if (condition.test(entryKey, entryValue))
			{
				copy.putInternal(entryKey, entryValue);
			}
		}
		return copy;
	}
	
	@Override
	public ImmutableMap<V, K> inverted()
	{
		HashMap<V, K> copy = new HashMap<V, K>(this.size);
		for (Entry<K, V> entry : this)
		{
			copy.putInternal(entry.getValue(), entry.getKey());
		}
		return copy;
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
