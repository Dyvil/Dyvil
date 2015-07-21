package dyvil.collection.immutable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import dyvil.collection.*;
import dyvil.collection.impl.AbstractHashMap;
import dyvil.math.MathUtils;

public class HashMap<K, V> extends AbstractHashMap<K, V>implements ImmutableMap<K, V>
{
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
		private HashEntry<K, V>[]	entries;
		private int					size;
		private int					threshold;
		
		public Builder()
		{
			this.entries = new HashEntry[DEFAULT_CAPACITY];
			this.threshold = 12;
		}
		
		public Builder(int capacity)
		{
			this.entries = new HashEntry[MathUtils.powerOfTwo(capacity)];
			this.threshold = capacity * 3 / 4;
		}
		
		protected void rehash()
		{
			HashEntry[] oldMap = this.entries;
			int oldCapacity = oldMap.length;
			
			// overflow-conscious code
			int newCapacity = (oldCapacity << 1) + 1;
			if (newCapacity - MAX_ARRAY_SIZE > 0)
			{
				if (oldCapacity == MAX_ARRAY_SIZE)
				{
					// Keep running with MAX_ARRAY_SIZE buckets
					return;
				}
				newCapacity = MAX_ARRAY_SIZE;
			}
			HashEntry[] newMap = new HashEntry[newCapacity];
			
			this.threshold = Math.min(newCapacity * 3 / 4, MAX_ARRAY_SIZE + 1);
			this.entries = newMap;
			
			for (int i = oldCapacity; i-- > 0;)
			{
				HashEntry e = oldMap[i];
				while (e != null)
				{
					int index = index(e.hash, newCapacity);
					HashEntry next = e.next;
					e.next = newMap[index];
					newMap[index] = e;
					e = next;
				}
			}
		}
		
		private void addEntry(int hash, K key, V value, int index)
		{
			HashEntry[] tab = this.entries;
			if (this.size >= this.threshold)
			{
				// Rehash the table if the threshold is exceeded
				this.rehash();
				
				tab = this.entries;
				hash = hash(key);
				index = index(hash, tab.length);
			}
			
			tab[index] = new HashEntry(key, value, hash, tab[index]);
			this.size++;
		}
		
		@Override
		public void put(K key, V value)
		{
			int hash = hash(key);
			int i = index(hash, this.entries.length);
			for (HashEntry<K, V> e = this.entries[i]; e != null; e = e.next)
			{
				Object k;
				if (e.hash == hash && ((k = e.key) == key || key.equals(k)))
				{
					e.value = value;
					return;
				}
			}
			
			this.addEntry(hash, key, value, i);
		}
		
		@Override
		public HashMap<K, V> build()
		{
			HashMap<K, V> map = new HashMap(this.size, this.entries);
			this.size = -1;
			return map;
		}
	}
	
	HashMap(int size, HashEntry[] entries)
	{
		this.entries = entries;
		this.size = size;
	}
	
	public HashMap(Map<? extends K, ? extends V> map)
	{
		super(map);
	}
	
	@Override
	public ImmutableMap<K, V> $plus(K key, V value)
	{
		Builder<K, V> builder = new Builder<K, V>(this.size + 1);
		builder.putAll(this);
		builder.put(key, value);
		return builder.build();
	}
	
	@Override
	public ImmutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map)
	{
		Builder<K, V> builder = new Builder<K, V>(this.size + map.size());
		builder.putAll(this);
		builder.putAll(map);
		return builder.build();
	}
	
	@Override
	public ImmutableMap<K, V> $minus$at(Object key)
	{
		Builder<K, V> builder = new Builder<K, V>(this.size);
		for (Entry<K, V> entry : this)
		{
			K entryKey = entry.getKey();
			if (!Objects.equals(entryKey, key))
			{
				builder.put(entryKey, entry.getValue());
			}
		}
		return builder.build();
	}
	
	@Override
	public ImmutableMap<K, V> $minus(Object key, Object value)
	{
		Builder<K, V> builder = new Builder<K, V>(this.size);
		for (Entry<K, V> entry : this)
		{
			K entryKey = entry.getKey();
			V entryValue = entry.getValue();
			if (!Objects.equals(entryKey, key) || !Objects.equals(entryValue, value))
			{
				builder.put(entryKey, entryValue);
			}
		}
		return builder.build();
	}
	
	@Override
	public ImmutableMap<K, V> $minus$colon(Object value)
	{
		Builder<K, V> builder = new Builder<K, V>(this.size);
		for (Entry<K, V> entry : this)
		{
			V entryValue = entry.getValue();
			if (!Objects.equals(entryValue, value))
			{
				builder.put(entry.getKey(), entryValue);
			}
		}
		return builder.build();
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Map<?, ?> map)
	{
		Builder<K, V> builder = new Builder<K, V>(this.size);
		for (Entry<K, V> entry : this)
		{
			K entryKey = entry.getKey();
			V entryValue = entry.getValue();
			if (!map.contains(entryKey, entryValue))
			{
				builder.put(entryKey, entryValue);
			}
		}
		return builder.build();
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Collection<?> keys)
	{
		Builder<K, V> builder = new Builder<K, V>(this.size);
		for (Entry<K, V> entry : this)
		{
			K entryKey = entry.getKey();
			if (!keys.contains(entryKey))
			{
				builder.put(entryKey, entry.getValue());
			}
		}
		return builder.build();
	}
	
	@Override
	public <U> ImmutableMap<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper)
	{
		Builder<K, U> builder = new Builder<K, U>(this.size);
		for (Entry<K, V> entry : this)
		{
			K entryKey = entry.getKey();
			U entryValue = mapper.apply(entryKey, entry.getValue());
			builder.put(entryKey, entryValue);
		}
		return builder.build();
	}
	
	@Override
	public <U, R> ImmutableMap<U, R> entryMapped(BiFunction<? super K, ? super V, ? extends Entry<? extends U, ? extends R>> mapper)
	{
		Builder<U, R> builder = new Builder<U, R>(this.size);
		for (Entry<K, V> entry : this)
		{
			Entry<? extends U, ? extends R> newEntry = mapper.apply(entry.getKey(), entry.getValue());
			if (newEntry != null)
			{
				builder.put(newEntry.getKey(), newEntry.getValue());
			}
		}
		return builder.build();
	}
	
	@Override
	public <U, R> ImmutableMap<U, R> flatMapped(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends U, ? extends R>>> mapper)
	{
		Builder<U, R> builder = new Builder<U, R>(this.size);
		for (Entry<K, V> entry : this)
		{
			for (Entry<? extends U, ? extends R> newEntry : mapper.apply(entry.getKey(), entry.getValue()))
			{
				builder.put(newEntry.getKey(), newEntry.getValue());
			}
		}
		return builder.build();
	}
	
	@Override
	public ImmutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		Builder<K, V> builder = new Builder<K, V>(this.size);
		for (Entry<K, V> entry : this)
		{
			K entryKey = entry.getKey();
			V entryValue = entry.getValue();
			if (condition.test(entryKey, entryValue)) {
				builder.put(entryKey, entryValue);
			}
		}
		return builder.build();
	}
	
	@Override
	public ImmutableMap<V, K> inverted()
	{
		Builder<V, K> builder = new Builder<V, K>(this.size);
		for (Entry<K, V> entry : this)
		{
			builder.put(entry.getValue(), entry.getKey());
		}
		return builder.build();
	}
	
	@Override
	public ImmutableMap<K, V> copy()
	{
		return new HashMap(this);
	}
	
	@Override
	public MutableMap<K, V> mutable()
	{
		return new dyvil.collection.mutable.HashMap<>(this);
	}
}
