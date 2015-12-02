package dyvil.collection.impl;

import dyvil.collection.Entry;
import dyvil.collection.Map;
import dyvil.lang.Type;
import dyvil.reflect.EnumReflection;
import dyvil.tuple.Tuple2;
import dyvil.util.None;
import dyvil.util.Option;
import dyvil.util.Some;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public abstract class AbstractEnumMap<K extends Enum<K>, V> implements Map<K, V>
{
	protected class EnumEntry implements Entry<K, V>
	{
		private static final long serialVersionUID = 4125489955668261409L;
		
		int index;
		
		EnumEntry(int index)
		{
			this.index = index;
		}
		
		@Override
		public K getKey()
		{
			return AbstractEnumMap.this.keys[this.index];
		}
		
		@Override
		public V getValue()
		{
			return (V) AbstractEnumMap.this.values[this.index];
		}
		
		@Override
		public String toString()
		{
			return AbstractEnumMap.this.keys[this.index] + " -> " + AbstractEnumMap.this.values[this.index];
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
	
	protected abstract class EnumIterator<E> implements Iterator<E>
	{
		private int     index;
		private boolean indexValid;
		
		@Override
		public boolean hasNext()
		{
			Object[] tab = AbstractEnumMap.this.values;
			for (int i = this.index; i < tab.length; i++)
			{
				Object key = tab[i];
				if (key != null)
				{
					this.index = i;
					return this.indexValid = true;
				}
			}
			this.index = tab.length;
			return false;
		}
		
		protected int nextIndex()
		{
			if (!this.indexValid && !this.hasNext())
			{
				throw new NoSuchElementException();
			}
			
			this.indexValid = false;
			return this.index++;
		}
		
		@Override
		public void remove()
		{
			if (this.index == 0)
			{
				throw new IllegalStateException();
			}
			
			AbstractEnumMap.this.removeAt(--this.index);
		}
	}
	
	private static final long serialVersionUID = 7946242151088885999L;
	
	protected transient Class<K> type;
	protected transient K[]      keys;
	protected transient Object[] values;
	protected transient int      size;
	
	protected AbstractEnumMap(Class<K> type, K[] keys, V[] values, int size)
	{
		this.type = type;
		this.keys = keys;
		this.values = values;
		this.size = size;
	}
	
	public AbstractEnumMap(Type<K> type)
	{
		this(type.getTheClass());
	}
	
	public AbstractEnumMap(Class<K> type)
	{
		this.keys = EnumReflection.getEnumConstants(type);
		this.values = new Object[this.keys.length];
		this.type = type;
	}
	
	public AbstractEnumMap(Map<K, V> map)
	{
		this(getKeyType(map));
		
		for (Entry<K, V> entry : map)
		{
			this.putInternal(entry.getKey(), entry.getValue());
		}
	}
	
	public AbstractEnumMap(AbstractEnumMap<K, V> map)
	{
		this.keys = map.keys;
		this.type = map.type;
		this.values = map.values.clone();
		this.size = map.size;
	}
	
	public AbstractEnumMap(Tuple2<K, V>... entries)
	{
		this(getKeyType(entries));
		
		for (Tuple2<K, V> entry : entries)
		{
			this.putInternal(entry._1, entry._2);
		}
	}
	
	private static <K extends Enum<K>> Class<K> getKeyType(Tuple2<K, ?>[] tuples)
	{
		for (Tuple2<K, ?> entry : tuples)
		{
			if (entry._1 != null)
			{
				return entry._1.getDeclaringClass();
			}
		}
		
		throw new IllegalArgumentException("Invalid Enum Map - Could not get Enum type");
	}
	
	private static <K extends Enum<K>> Class<K> getKeyType(Map<K, ?> map)
	{
		for (Iterator<K> keys = map.keyIterator(); keys.hasNext(); )
		{
			K key = keys.next();
			if (key != null)
			{
				return key.getDeclaringClass();
			}
		}
		throw new IllegalArgumentException("Invalid Enum Map - Could not get Enum type");
	}
	
	protected void putInternal(K key, V value)
	{
		int index = key.ordinal();
		
		if (this.values[index] == null)
		{
			this.size++;
		}
		this.values[index] = value;
	}
	
	protected static boolean checkType(Class<?> type, Object key)
	{
		return key != null && key.getClass() == type;
	}
	
	protected static int index(Object key)
	{
		return ((Enum) key).ordinal();
	}
	
	@Override
	public int size()
	{
		return this.size;
	}
	
	@Override
	public Iterator<Entry<K, V>> iterator()
	{
		return new EnumIterator<Entry<K, V>>()
		{
			@Override
			public Entry<K, V> next()
			{
				return new EnumEntry(this.nextIndex());
			}
			
			@Override
			public String toString()
			{
				return "EntryIterator(" + AbstractEnumMap.this + ")";
			}
		};
	}
	
	@Override
	public Iterator<K> keyIterator()
	{
		return new EnumIterator<K>()
		{
			@Override
			public K next()
			{
				return AbstractEnumMap.this.keys[this.nextIndex()];
			}
			
			@Override
			public String toString()
			{
				return "KeyIterator(" + AbstractEnumMap.this + ")";
			}
		};
	}
	
	@Override
	public Iterator<V> valueIterator()
	{
		return new EnumIterator<V>()
		{
			@Override
			public V next()
			{
				return (V) AbstractEnumMap.this.values[this.nextIndex()];
			}
			
			@Override
			public String toString()
			{
				return "ValueIterator(" + AbstractEnumMap.this + ")";
			}
		};
	}
	
	protected abstract void removeAt(int index);
	
	@Override
	public boolean containsKey(Object key)
	{
		if (!checkType(this.type, key))
		{
			return false;
		}
		
		return this.values[index(key)] != null;
	}
	
	@Override
	public boolean contains(Object key, Object value)
	{
		if (!checkType(this.type, key))
		{
			return false;
		}
		
		Object v = this.values[((Enum) key).ordinal()];
		return Objects.equals(v, value);
	}
	
	@Override
	public boolean containsValue(Object value)
	{
		int len = this.values.length;
		for (int i = 0; i < len; i++)
		{
			if (Objects.equals(this.values[i], value))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public V get(Object key)
	{
		if (!checkType(this.type, key))
		{
			return null;
		}
		
		return (V) this.values[index(key)];
	}
	
	@Override
	public Option<V> getOption(Object key)
	{
		if (!checkType(this.type, key))
		{
			return None.instance;
		}
		
		return new Some(this.values[index(key)]);
	}
	
	@Override
	public java.util.Map<K, V> toJava()
	{
		java.util.EnumMap<K, V> map = new java.util.EnumMap<>(this.type);
		for (int i = 0; i < this.keys.length; i++)
		{
			V value = (V) this.values[i];
			if (value != null)
			{
				map.put(this.keys[i], value);
			}
		}
		return map;
	}
	
	@Override
	public String toString()
	{
		return Map.mapToString(this);
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
		
		out.writeObject(this.type);
		out.writeInt(this.size);
		
		int entriesToBeWritten = this.size;
		for (int i = 0; entriesToBeWritten > 0; i++)
		{
			if (this.values[i] != null)
			{
				out.writeObject(this.keys[i]);
				out.writeObject(this.values[i]);
				entriesToBeWritten--;
			}
		}
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		
		this.type = (Class<K>) in.readObject();
		this.keys = EnumReflection.getEnumConstants(this.type);
		this.values = new Object[this.keys.length];
		
		int size = in.readInt();
		
		for (int i = 0; i < size; i++)
		{
			K key = (K) in.readObject();
			V value = (V) in.readObject();
			this.putInternal(key, value);
		}
	}
}
