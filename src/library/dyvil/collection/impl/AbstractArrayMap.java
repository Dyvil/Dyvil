package dyvil.collection.impl;

import dyvil.collection.*;
import dyvil.util.None;
import dyvil.util.Option;
import dyvil.util.Some;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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

	protected static final int DEFAULT_CAPACITY = 16;

	protected transient int      size;
	protected transient Object[] keys;
	protected transient Object[] values;

	public AbstractArrayMap()
	{
		this.keys = new Object[DEFAULT_CAPACITY];
		this.values = new Object[DEFAULT_CAPACITY];
	}

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

	public AbstractArrayMap(K[] keys, V[] values, @SuppressWarnings("UnusedParameters") boolean trusted)
	{
		this.keys = keys;
		this.values = values;
		this.size = keys.length;
	}

	public AbstractArrayMap(K[] keys, V[] values, int size, @SuppressWarnings("UnusedParameters") boolean trusted)
	{
		this.keys = keys;
		this.values = values;
		this.size = size;
	}

	public AbstractArrayMap(Entry<? extends K, ? extends V>[] entries)
	{
		this(entries.length);
		for (Entry<? extends K, ? extends V> entry : entries)
		{
			this.putInternal(entry.getKey(), entry.getValue());
		}
	}

	public AbstractArrayMap(Iterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		this();
		this.loadEntries(iterable);
	}

	public AbstractArrayMap(SizedIterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		this(iterable.size());
		this.loadEntries(iterable);
	}

	public AbstractArrayMap(Set<? extends Entry<? extends K, ? extends V>> set)
	{
		this(set.size());
		this.loadDistinctEntries(set);
	}

	public AbstractArrayMap(Map<? extends K, ? extends V> map)
	{
		this(map.size());
		this.loadDistinctEntries(map);
	}

	public AbstractArrayMap(AbstractArrayMap<? extends K, ? extends V> map)
	{
		this.size = map.size;
		this.keys = map.keys.clone();
		this.values = map.values.clone();
	}

	private void loadEntries(Iterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		for (Entry<? extends K, ? extends V> entry : iterable)
		{
			this.putInternal(entry.getKey(), entry.getValue());
		}
	}

	private void loadDistinctEntries(Iterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		int index = 0;
		for (Entry<? extends K, ? extends V> entry : iterable)
		{
			this.keys[index] = entry.getKey();
			this.values[index] = entry.getValue();
			index++;
		}
		this.size = index;
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
			System.arraycopy(this.values, 0, newValues, 0, index);
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
					return new Some<>((V) this.values[i]);
				}
			}
			return (Option<V>) None.instance;
		}
		for (int i = 0; i < this.size; i++)
		{
			if (key.equals(this.keys[i]))
			{
				return new Some<>((V) this.values[i]);
			}
		}
		return (Option<V>) None.instance;
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
	public <RK, RV> MutableMap<RK, RV> emptyCopy()
	{
		return new dyvil.collection.mutable.ArrayMap<>();
	}

	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy(int capacity)
	{
		return new dyvil.collection.mutable.ArrayMap<>(capacity);
	}

	@Override
	public MutableMap<K, V> mutableCopy()
	{
		return new dyvil.collection.mutable.ArrayMap<>(this);
	}

	@Override
	public ImmutableMap<K, V> immutableCopy()
	{
		return new dyvil.collection.immutable.ArrayMap<>(this);
	}

	@Override
	public <RK, RV> ImmutableMap.Builder<RK, RV> immutableBuilder()
	{
		return dyvil.collection.immutable.ArrayMap.builder();
	}

	@Override
	public <RK, RV> ImmutableMap.Builder<RK, RV> immutableBuilder(int capacity)
	{
		return dyvil.collection.immutable.ArrayMap.builder(capacity);
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
			return Map.EMPTY_STRING;
		}

		final StringBuilder builder = new StringBuilder(Map.START_STRING);
		builder.append(this.keys[0]).append(Map.KEY_VALUE_SEPARATOR_STRING).append(this.values[0]);
		for (int i = 1; i < this.size; i++)
		{
			builder.append(Map.ENTRY_SEPARATOR_STRING);
			builder.append(this.keys[i]).append(Map.KEY_VALUE_SEPARATOR_STRING).append(this.values[i]);
		}
		return builder.append(END_STRING).toString();
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
