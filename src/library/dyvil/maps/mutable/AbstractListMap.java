package dyvil.maps.mutable;

import java.util.*;

public abstract class AbstractListMap<K, V> extends AbstractMap<K, V> implements ListMap<K, V>
{
	private static class ListMapEntry<K, V> implements Map.Entry<K, V>
	{
		K	key;
		V	value;
		
		public ListMapEntry(K key, V value)
		{
			this.key = key;
			this.value = value;
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
		public V setValue(V value)
		{
			V oldValue = this.value;
			this.value = value;
			return oldValue;
		}
	}
	
	protected List<ListMapEntry<K, V>>	entries;
	
	@Override
	public int size()
	{
		return this.entries.size();
	}
	
	@Override
	public boolean isEmpty()
	{
		return this.entries.isEmpty();
	}
	
	@Override
	public void clear()
	{
		this.entries.clear();
	}
	
	@Override
	public boolean containsKey(Object key)
	{
		return this.keySet().contains(key);
	}
	
	@Override
	public boolean containsValue(Object value)
	{
		return this.values().contains(value);
	}
	
	@Override
	public Set entrySet()
	{
		return new HashSet(this.entries);
	}
	
	protected ListMapEntry<K, V> getEntry(Object key)
	{
		for (ListMapEntry<K, V> entry : this.entries)
		{
			if (Objects.equals(key, entry.key))
			{
				return entry;
			}
		}
		return null;
	}
	
	@Override
	public V get(Object key)
	{
		ListMapEntry<K, V> entry = this.getEntry(key);
		return entry != null ? entry.value : null;
	}
	
	@Override
	public V put(K key, V value)
	{
		ListMapEntry<K, V> entry = this.getEntry(key);
		if (entry != null)
		{
			return entry.setValue(value);
		}
		entry = new ListMapEntry<K, V>(key, value);
		this.entries.add(entry);
		return null;
	}
	
	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		for (K k : m.keySet())
		{
			this.put(k, m.get(k));
		}
	}
	
	@Override
	public V remove(Object key)
	{
		Iterator<ListMapEntry<K, V>> iterator = this.entries.iterator();
		ListMapEntry<K, V> entry = null;
		
		while (iterator.hasNext())
		{
			entry = iterator.next();
			if (Objects.equals(key, entry.key))
			{
				iterator.remove();
				return entry.value;
			}
		}
		
		return null;
	}
}
