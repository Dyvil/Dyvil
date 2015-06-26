package dyvil.collection.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import dyvil.lang.Entry;
import dyvil.lang.Map;
import dyvil.lang.Type;

import sun.misc.SharedSecrets;

public abstract class AbstractEnumMap<K extends Enum<K>, V> implements Map<K, V>
{
	protected class EnumEntry implements Entry<K, V>
	{
		int	index;
		
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
		private int		index;
		private boolean	indexValid;
		
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
	}
	
	protected Class<K>	type;
	protected K[]		keys;
	protected Object[]	values;
	protected int		size;
	
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
		this.keys = SharedSecrets.getJavaLangAccess().getEnumConstantsShared(type);
		this.values = new Object[this.keys.length];
		this.type = type;
	}
	
	protected boolean checkType(Object key)
	{
		return key != null && key.getClass() == this.type;
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
	
	@Override
	public boolean containsKey(Object key)
	{
		if (!this.checkType(key))
		{
			return false;
		}
		
		return this.values[index(key)] != null;
	}
	
	@Override
	public boolean contains(Object key, Object value)
	{
		if (!this.checkType(key))
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
	public V get(K key)
	{
		if (!this.checkType(key))
		{
			return null;
		}
		
		return (V) this.values[index(key)];
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
}
