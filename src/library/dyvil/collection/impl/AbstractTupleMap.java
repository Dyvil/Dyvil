package dyvil.collection.impl;

import dyvil.collection.Entry;
import dyvil.collection.ImmutableMap;
import dyvil.collection.Map;
import dyvil.collection.MutableMap;
import dyvil.tuple.Tuple2;
import dyvil.util.None;
import dyvil.util.Option;
import dyvil.util.Some;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class AbstractTupleMap<K, V> implements Map<K, V>
{
	private static final long serialVersionUID = 1636602530347500387L;
	
	protected static final int DEFAULT_CAPACITY = 10;
	
	protected transient int            size;
	protected transient Tuple2<K, V>[] entries;
	
	protected AbstractTupleMap(int capacity)
	{
		this.entries = (Tuple2<K, V>[]) new Tuple2[capacity];
	}

	@SafeVarargs
	public AbstractTupleMap(Entry<K, V>... entries)
	{
		this(entries.length);
		this.size = entries.length;
		for (int i = 0; i < entries.length; i++)
		{
			this.entries[i] = entries[i].toTuple();
		}
	}
	
	@SafeVarargs
	public AbstractTupleMap(Tuple2<K, V>... entries)
	{
		this.size = entries.length;
		this.entries = (Tuple2<K, V>[]) new Tuple2[this.size];
		System.arraycopy(entries, 0, this.entries, 0, this.size);
	}
	
	public AbstractTupleMap(Tuple2<K, V>[] entries, int size)
	{
		this.size = size;
		this.entries = (Tuple2<K, V>[]) new Tuple2[size];
		System.arraycopy(entries, 0, this.entries, 0, size);
	}
	
	public AbstractTupleMap(Tuple2<K, V>[] entries, @SuppressWarnings("UnusedParameters") boolean trusted)
	{
		this.size = entries.length;
		this.entries = entries;
	}
	
	public AbstractTupleMap(Tuple2<K, V>[] entries, int size, @SuppressWarnings("UnusedParameters") boolean trusted)
	{
		this.size = size;
		this.entries = entries;
	}
	
	public AbstractTupleMap(Map<K, V> map)
	{
		this.size = map.size();
		this.entries = (Tuple2<K, V>[]) new Tuple2[this.size];
		
		int index = 0;
		for (Entry<K, V> entry : map)
		{
			this.entries[index++] = entry.toTuple();
		}
	}
	
	public AbstractTupleMap(AbstractTupleMap<K, V> map)
	{
		this.size = map.size;
		this.entries = map.entries.clone();
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
	public Iterator<Entry<K, V>> iterator()
	{
		return new Iterator<Entry<K, V>>()
		{
			private int index;
			
			@Override
			public boolean hasNext()
			{
				return this.index < AbstractTupleMap.this.size;
			}
			
			@Override
			public Entry<K, V> next()
			{
				return AbstractTupleMap.this.entries[this.index++];
			}
			
			@Override
			public void remove()
			{
				if (this.index <= 0)
				{
					throw new IllegalStateException();
				}
				AbstractTupleMap.this.removeAt(--this.index);
			}
		};
	}
	
	@Override
	public Iterator<K> keyIterator()
	{
		return new Iterator<K>()
		{
			private int index;
			
			@Override
			public boolean hasNext()
			{
				return this.index < AbstractTupleMap.this.size;
			}
			
			@Override
			public K next()
			{
				return AbstractTupleMap.this.entries[this.index++]._1;
			}
			
			@Override
			public void remove()
			{
				if (this.index <= 0)
				{
					throw new IllegalStateException();
				}
				AbstractTupleMap.this.removeAt(--this.index);
			}
		};
	}
	
	@Override
	public Iterator<V> valueIterator()
	{
		return new Iterator<V>()
		{
			private int index;
			
			@Override
			public boolean hasNext()
			{
				return this.index < AbstractTupleMap.this.size;
			}
			
			@Override
			public V next()
			{
				return AbstractTupleMap.this.entries[this.index++]._2;
			}
			
			@Override
			public void remove()
			{
				if (this.index <= 0)
				{
					throw new IllegalStateException();
				}
				AbstractTupleMap.this.removeAt(--this.index);
			}
		};
	}
	
	protected abstract void removeAt(int index);
	
	protected final V putInternal(Tuple2<K, V> tuple)
	{
		K key = tuple._1;
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (Objects.equals(key, entry._1))
			{
				V oldValue = entry._2;
				this.entries[i] = tuple;
				return oldValue;
			}
		}
		
		this.putNew(tuple);
		return null;
	}
	
	protected final void putNew(Tuple2<K, V> tuple)
	{
		int index = this.size++;
		if (index >= this.entries.length)
		{
			int newCapacity = (int) (this.size * 1.1F);
			final Tuple2<K, V>[] newEntries = (Tuple2<K, V>[]) new Tuple2[newCapacity];
			System.arraycopy(this.entries, 0, newEntries, 0, index);
			this.entries = newEntries;
		}
		this.entries[index] = tuple;
	}
	
	@Override
	public void forEach(Consumer<? super Entry<K, V>> action)
	{
		for (int i = 0; i < this.size; i++)
		{
			action.accept(this.entries[i]);
		}
	}
	
	@Override
	public void forEach(BiConsumer<? super K, ? super V> action)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			action.accept(entry._1, entry._2);
		}
	}
	
	@Override
	public void forEachKey(Consumer<? super K> action)
	{
		for (int i = 0; i < this.size; i++)
		{
			action.accept(this.entries[i]._1);
		}
	}
	
	@Override
	public void forEachValue(Consumer<? super V> action)
	{
		for (int i = 0; i < this.size; i++)
		{
			action.accept(this.entries[i]._2);
		}
	}
	
	@Override
	public boolean containsKey(Object key)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (key == entry._1 || key != null && key.equals(entry._1))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean contains(Object key, Object value)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (key == entry._1 || key != null && key.equals(entry._1))
			{
				if (value == entry._2 || value != null && value.equals(entry._2))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean containsValue(Object value)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (value == entry._2 || value != null && value.equals(entry._2))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public V get(Object key)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (key == entry._1 || key != null && key.equals(entry._1))
			{
				return entry._2;
			}
		}
		return null;
	}
	
	@Override
	public Option<V> getOption(Object key)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (key == entry._1 || key != null && key.equals(entry._1))
			{
				return new Some<>(entry._2);
			}
		}
		return (Option<V>) None.instance;
	}
	
	@Override
	public Entry<K, V>[] toArray()
	{
		final Tuple2<K, V>[] array = (Tuple2<K, V>[]) new Tuple2[this.size];
		System.arraycopy(this.entries, 0, array, 0, this.size);
		return array;
	}
	
	@Override
	public void toArray(int index, Entry<K, V>[] store)
	{
		System.arraycopy(this.entries, 0, store, index, this.size);
	}
	
	@Override
	public void toKeyArray(int index, Object[] store)
	{
		for (int i = 0; i < this.size; i++)
		{
			store[index++] = this.entries[i]._1;
		}
	}
	
	@Override
	public void toValueArray(int index, Object[] store)
	{
		for (int i = 0; i < this.size; i++)
		{
			store[index++] = this.entries[i]._2;
		}
	}

	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy()
	{
		return new dyvil.collection.mutable.TupleMap<>();
	}

	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy(int capacity)
	{
		return new dyvil.collection.mutable.TupleMap<>(capacity);
	}

	@Override
	public MutableMap<K, V> mutableCopy()
	{
		return new dyvil.collection.mutable.TupleMap<>(this);
	}

	@Override
	public ImmutableMap<K, V> immutableCopy()
	{
		return new dyvil.collection.immutable.TupleMap<>(this);
	}

	@Override
	public <RK, RV> ImmutableMap.Builder<RK, RV> immutableBuilder()
	{
		return dyvil.collection.immutable.TupleMap.builder();
	}

	@Override
	public <RK, RV> ImmutableMap.Builder<RK, RV> immutableBuilder(int capacity)
	{
		return dyvil.collection.immutable.TupleMap.builder(capacity);
	}
	
	@Override
	public java.util.Map<K, V> toJava()
	{
		java.util.LinkedHashMap<K, V> map = new java.util.LinkedHashMap<>(this.size);
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			map.put(entry._1, entry._2);
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
		Tuple2<K, V> entry = this.entries[0];

		builder.append(entry._1).append(Map.KEY_VALUE_SEPARATOR_STRING).append(entry._2);
		for (int i = 1; i < this.size; i++)
		{
			entry = this.entries[i];
			builder.append(Map.START_STRING).append(entry._1).append(Map.KEY_VALUE_SEPARATOR_STRING).append(entry._2);
		}
		return builder.append(Map.END_STRING).toString();
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
			out.writeObject(this.entries[i]);
		}
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		
		this.size = in.readInt();
		this.entries = (Tuple2<K, V>[]) new Tuple2[this.size];
		for (int i = 0; i < this.size; i++)
		{
			this.entries[i] = (Tuple2<K, V>) in.readObject();
		}
	}
}
