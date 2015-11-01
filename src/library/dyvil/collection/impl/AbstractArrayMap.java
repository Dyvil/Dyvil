package dyvil.collection.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import dyvil.collection.Entry;
import dyvil.collection.Map;
import dyvil.tuple.Tuple2;
import dyvil.util.None;
import dyvil.util.Option;
import dyvil.util.Some;

public abstract class AbstractArrayMap<K, V> implements Map<K, V>
{
	protected class ArrayMapEntry implements Entry<K, V>
	{
		private static final long serialVersionUID = -967348930318928118L;
		
		private int index;
		
		public ArrayMapEntry(int index)
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
	
	private static final long serialVersionUID = -4958236535555733690L;
	
	protected static final int DEFAULT_CAPACITY = 10;
	
	protected transient int			size;
	protected transient Object[]	keys;
	protected transient Object[]	values;
	
	protected AbstractArrayMap(int capacity)
	{
		this.keys = new Object[capacity];
		this.values = new Object[capacity];
	}
	
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
	
	public AbstractArrayMap(AbstractArrayMap<K, V> map)
	{
		this.size = map.size;
		this.keys = map.keys.clone();
		this.values = map.values.clone();
	}
	
	public AbstractArrayMap(Tuple2<K, V>... tuples)
	{
		int len = tuples.length;
		Object[] keys = this.keys = new Object[len];
		Object[] values = this.values = new Object[len];
		
		int size = 0;
		
		outer:
		for (int i = 0; i < len; i++)
		{
			Tuple2 entry = tuples[i];
			Object key = entry._1;
			for (int j = 0; j < size; j++)
			{
				if (Objects.equals(key, keys[j]))
				{
					values[j] = entry._2;
					continue outer;
				}
			}
			
			keys[size] = key;
			values[size] = entry._2;
			size++;
		}
		
		this.size = size;
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
	
	protected abstract class ArrayIterator<R> implements Iterator<R>
	{
		protected int index;
		
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
				return new ArrayMapEntry(this.index++);
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
	
	protected void putNew(K key, V value)
	{
		int index = this.size++;
		if (index >= this.keys.length)
		{
			int newCapacity = (int) (this.size * 1.1F);
			Object[] newKeys = new Object[newCapacity];
			Object[] newValues = new Object[newCapacity];
			System.arraycopy(this.keys, 0, newKeys, 0, index);
			System.arraycopy(this.values, 0, newValues, 0, newCapacity);
			this.keys = newKeys;
			this.values = newValues;
		}
		this.keys[index] = key;
		this.values[index] = value;
	}
	
	protected V putInternal(K key, V value)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (Objects.equals(key, this.keys[i]))
			{
				V oldValue = (V) this.values[i];
				this.values[i] = value;
				return oldValue;
			}
		}
		
		this.putNew(key, value);
		return null;
	}
	
	protected abstract void removeAt(int index);
	
	@Override
	public void forEach(Consumer<? super Entry<K, V>> action)
	{
		for (int i = 0; i < this.size; i++)
		{
			action.accept(new ArrayMapEntry(i));
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
	
	@Override
	public Option<V> getOption(Object key)
	{
		if (key == null)
		{
			for (int i = 0; i < this.size; i++)
			{
				if (this.keys[i] == null)
				{
					return new Some(this.values[i]);
				}
			}
			return None.instance;
		}
		for (int i = 0; i < this.size; i++)
		{
			if (key.equals(this.keys[i]))
			{
				return new Some(this.values[i]);
			}
		}
		return None.instance;
	}
	
	@Override
	public void toArray(int index, Entry<K, V>[] store)
	{
		for (int i = 0; i < this.size; i++)
		{
			store[index++] = new ArrayMapEntry(i);
		}
	}
	
	@Override
	public void toKeyArray(int index, Object[] store)
	{
		System.arraycopy(this.keys, 0, store, index, this.size);
	}
	
	@Override
	public void toValueArray(int index, Object[] store)
	{
		System.arraycopy(this.values, 0, store, index, this.size);
	}
	
	@Override
	public java.util.Map<K, V> toJava()
	{
		java.util.LinkedHashMap<K, V> map = new java.util.LinkedHashMap<>(this.size);
		for (int i = 0; i < this.size; i++)
		{
			map.put((K) this.keys[i], (V) this.values[i]);
		}
		return map;
	}
	
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
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
		
		out.writeInt(this.size);
		
		for (int i = 0; i < this.size; i++)
		{
			out.writeObject(this.keys[i]);
			out.writeObject(this.values[i]);
		}
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		
		this.size = in.readInt();
		this.keys = new Object[this.size];
		this.values = new Object[this.size];
		
		for (int i = 0; i < this.size; i++)
		{
			this.keys[i] = in.readObject();
			this.values[i] = in.readObject();
		}
	}
}
