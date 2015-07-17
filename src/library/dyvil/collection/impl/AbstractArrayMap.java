package dyvil.collection.impl;

import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import dyvil.collection.Entry;
import dyvil.collection.Map;
import dyvil.tuple.Tuple2;

public abstract class AbstractArrayMap<K, V> implements Map<K, V>
{
	protected class ArrayEntry implements Entry<K, V>
	{
		private int	index;
		
		public ArrayEntry(int index)
		{
			this.index = index;
		}
		
		@Override
		public K getKey()
		{
			return (K) AbstractArrayMap.this.keys[this.index];
		}
		
		@Override
		public V getValue()
		{
			return (V) AbstractArrayMap.this.values[this.index];
		}
		
		@Override
		public String toString()
		{
			return Entry.entryToString(this);
		}
		
		@Override
		public boolean equals(Object obj)
		{
			return Entry.entryEquals(this, obj);
		}
		
		@Override
		public int hashCode()
		{
			return Entry.entryHashCode(this);
		}
	}
	
	protected int		size;
	protected Object[]	keys;
	protected Object[]	values;
	
	public AbstractArrayMap(K[] keys, V[] values)
	{
		int size = keys.length;
		if (size != values.length)
		{
			throw new IllegalArgumentException("keys.length != values.length");
		}
		
		this.keys = new Object[size];
		System.arraycopy(keys, 0, this.keys, 0, size);
		this.values = new Object[size];
		System.arraycopy(values, 0, this.values, 0, size);
		this.size = size;
	}
	
	public AbstractArrayMap(K[] keys, V[] values, int size)
	{
		if (keys.length < size)
		{
			throw new IllegalArgumentException("keys.length < size");
		}
		if (values.length < size)
		{
			throw new IllegalArgumentException("values.length < size");
		}
		
		this.keys = new Object[size];
		System.arraycopy(keys, 0, this.keys, 0, size);
		this.values = new Object[size];
		System.arraycopy(values, 0, this.values, 0, size);
		this.size = size;
	}
	
	public AbstractArrayMap(Object[] keys, Object[] values, boolean trusted)
	{
		this.keys = keys;
		this.values = values;
		this.size = keys.length;
	}
	
	public AbstractArrayMap(Object[] keys, Object[] values, int size, boolean trusted)
	{
		this.keys = keys;
		this.values = values;
		this.size = size;
	}
	
	public AbstractArrayMap(Map<K, V> map)
	{
		this.size = map.size();
		this.keys = new Object[this.size];
		this.values = new Object[this.size];
		
		int index = 0;
		for (Entry<K, V> entry : map)
		{
			this.keys[index] = entry.getKey();
			this.values[index] = entry.getValue();
			index++;
		}
	}
	
	@Override
	public int size()
	{
		return this.size;
	}
	
	@Override
	public boolean isEmpty()
	{
		return this.size == 0;
	}
	
	@Override
	public void forEach(Consumer<? super Entry<K, V>> action)
	{
		for (int i = 0; i < this.size; i++)
		{
			action.accept(new Tuple2(this.keys[i], this.values[i]));
		}
	}
	
	@Override
	public void forEach(BiConsumer<? super K, ? super V> action)
	{
		for (int i = 0; i < this.size; i++)
		{
			action.accept((K) this.keys[i], (V) this.values[i]);
		}
	}
	
	@Override
	public void forEachKey(Consumer<? super K> action)
	{
		for (int i = 0; i < this.size; i++)
		{
			action.accept((K) this.keys[i]);
		}
	}
	
	@Override
	public void forEachValue(Consumer<? super V> action)
	{
		for (int i = 0; i < this.size; i++)
		{
			action.accept((V) this.values[i]);
		}
	}
	
	@Override
	public boolean containsKey(Object key)
	{
		if (key == null)
		{
			for (int i = 0; i < this.size; i++)
			{
				if (this.keys[i] == null)
				{
					return true;
				}
			}
			return false;
		}
		for (int i = 0; i < this.size; i++)
		{
			if (key.equals(this.keys[i]))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean contains(Object key, Object value)
	{
		if (key == null)
		{
			for (int i = 0; i < this.size; i++)
			{
				if (this.keys[i] == null && (value == null ? this.values[i] == null : value.equals(this.values[i])))
				{
					return true;
				}
			}
			return false;
		}
		for (int i = 0; i < this.size; i++)
		{
			if (key.equals(this.keys[i]) && (value == null ? this.values[i] == null : value.equals(this.values[i])))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean containsValue(Object value)
	{
		if (value == null)
		{
			for (int i = 0; i < this.size; i++)
			{
				if (this.values[i] == null)
				{
					return true;
				}
			}
			return false;
		}
		for (int i = 0; i < this.size; i++)
		{
			if (value.equals(this.values[i]))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public V get(Object key)
	{
		if (key == null)
		{
			for (int i = 0; i < this.size; i++)
			{
				if (this.keys[i] == null)
				{
					return (V) this.values[i];
				}
			}
			return null;
		}
		for (int i = 0; i < this.size; i++)
		{
			if (key.equals(this.keys[i]))
			{
				return (V) this.values[i];
			}
		}
		return null;
	}
	
	protected abstract class ArrayIterator<R> implements Iterator<R>
	{
		protected int	index;
		
		@Override
		public boolean hasNext()
		{
			return this.index < AbstractArrayMap.this.size;
		}
		
		@Override
		public void remove()
		{
			if (this.index == 0)
			{
				throw new IllegalStateException();
			}
			
			AbstractArrayMap.this.removeAt(this.index--);
		}
	}
	
	@Override
	public Iterator<Entry<K, V>> iterator()
	{
		return new ArrayIterator<Entry<K, V>>()
		{
			@Override
			public Entry<K, V> next()
			{
				return new ArrayEntry(this.index++);
			}
		};
	}
	
	@Override
	public Iterator<K> keyIterator()
	{
		return new ArrayIterator<K>()
		{
			@Override
			public K next()
			{
				return (K) AbstractArrayMap.this.keys[this.index++];
			}
		};
	}
	
	@Override
	public Iterator<V> valueIterator()
	{
		return new ArrayIterator<V>()
		{
			@Override
			public V next()
			{
				return (V) AbstractArrayMap.this.values[this.index++];
			}
		};
	}
	
	protected abstract void removeAt(int index);
	
	@Override
	public String toString()
	{
		if (this.size <= 0)
		{
			return "[]";
		}
		
		StringBuilder builder = new StringBuilder("[ ");
		builder.append(this.keys[0]).append(" -> ").append(this.values[0]);
		for (int i = 1; i < this.size; i++)
		{
			builder.append(", ");
			builder.append(this.keys[i]).append(" -> ").append(this.values[i]);
		}
		return builder.append(" ]").toString();
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
