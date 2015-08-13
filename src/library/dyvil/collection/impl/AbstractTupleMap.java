package dyvil.collection.impl;

import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import dyvil.collection.Entry;
import dyvil.collection.Map;
import dyvil.tuple.Tuple2;
import dyvil.util.None;
import dyvil.util.Option;
import dyvil.util.Some;

public abstract class AbstractTupleMap<K, V> implements Map<K, V>
{
	protected int				size;
	protected Tuple2<K, V>[]	entries;
	
	public AbstractTupleMap(Tuple2<K, V>[] entries)
	{
		this.size = entries.length;
		this.entries = new Tuple2[this.size];
		System.arraycopy(entries, 0, this.entries, 0, this.size);
	}
	
	public AbstractTupleMap(Tuple2<K, V>[] entries, int size)
	{
		this.size = size;
		this.entries = new Tuple2[size];
		System.arraycopy(entries, 0, this.entries, 0, size);
	}
	
	public AbstractTupleMap(Tuple2<K, V>[] entries, boolean trusted)
	{
		this.size = entries.length;
		this.entries = entries;
	}
	
	public AbstractTupleMap(Tuple2<K, V>[] entries, int size, boolean trusted)
	{
		this.size = size;
		this.entries = entries;
	}
	
	public AbstractTupleMap(Map<K, V> map)
	{
		this.size = map.size();
		this.entries = new Tuple2[this.size];
		
		int index = 0;
		for (Entry<K, V> entry : map)
		{
			this.entries[index++] = entry.toTuple();
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
				return new Some(entry._2);
			}
		}
		return None.instance;
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
			return "[]";
		}
		
		StringBuilder builder = new StringBuilder("[ ");
		Tuple2<K, V> entry = this.entries[0];
		builder.append(entry._1).append(" -> ").append(entry._2);
		for (int i = 1; i < this.size; i++)
		{
			entry = this.entries[i];
			builder.append(", ").append(entry._1).append(" -> ").append(entry._2);
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
