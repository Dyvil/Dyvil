package dyvil.collection.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import dyvil.collection.Entry;
import dyvil.collection.Map;
import dyvil.math.MathUtils;

public abstract class AbstractHashMap<K, V> implements Map<K, V>
{
	protected static final class HashEntry<K, V> implements Entry<K, V>
	{
		public K			key;
		public V			value;
		public int			hash;
		public HashEntry	next;
		
		public HashEntry(K key, V value, int hash)
		{
			this.key = key;
			this.value = value;
			this.hash = hash;
		}
		
		public HashEntry(K key, V value, int hash, HashEntry next)
		{
			this.key = key;
			this.value = value;
			this.hash = hash;
			this.next = next;
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
		public String toString()
		{
			return this.key + " -> " + this.value;
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
	
	protected abstract class EntryIterator<E> implements Iterator<E>
	{
		HashEntry<K, V>	next;		// next entry to return
		HashEntry<K, V>	current;	// current entry
		int				index;		// current slot
		
		EntryIterator()
		{
			HashEntry<K, V>[] t = AbstractHashMap.this.entries;
			this.current = this.next = null;
			this.index = 0;
			// advance to first entry
			if (t != null && AbstractHashMap.this.size > 0)
			{
				do
				{
				}
				while (this.index < t.length && (this.next = t[this.index++]) == null);
			}
		}
		
		@Override
		public final boolean hasNext()
		{
			return this.next != null;
		}
		
		final HashEntry<K, V> nextEntry()
		{
			HashEntry<K, V>[] t;
			HashEntry<K, V> e = this.next;
			if (e == null)
			{
				throw new NoSuchElementException();
			}
			if ((this.next = (this.current = e).next) == null && (t = AbstractHashMap.this.entries) != null)
			{
				do
				{
				}
				while (this.index < t.length && (this.next = t[this.index++]) == null);
			}
			return e;
		}
		
		@Override
		public final void remove()
		{
			HashEntry<K, V> e = this.current;
			if (e == null)
			{
				throw new IllegalStateException();
			}
			
			AbstractHashMap.this.size--;
			this.current = null;
			int index = index(e.hash, AbstractHashMap.this.entries.length);
			HashEntry<K, V> entry = AbstractHashMap.this.entries[index];
			if (entry == e)
			{
				AbstractHashMap.this.entries[index] = e.next;
			}
			else
			{
				HashEntry<K, V> prev;
				do
				{
					prev = entry;
					entry = entry.next;
				}
				while (entry != e);
				
				prev.next = e.next;
			}
		}
	}
	
	public static final float	GROWTH_FACTOR		= 1.0625F;
	public static final int		DEFAULT_CAPACITY	= 16;
	public static final float	DEFAULT_LOAD_FACTOR	= 0.75F;
	public static final int		MAX_ARRAY_SIZE		= Integer.MAX_VALUE - 8;
	
	protected int			size;
	protected HashEntry[]	entries;
	
	public AbstractHashMap()
	{
	}
	
	public AbstractHashMap(Map<? extends K, ? extends V> map)
	{
		int size = map.size();
		int length = MathUtils.powerOfTwo(grow(size));
		this.entries = new HashEntry[length];
		this.size = size;
		
		outer:
		for (Entry<? extends K, ? extends V> entry : map)
		{
			K key = entry.getKey();
			V value = entry.getValue();
			
			int hash = hash(key);
			int i = index(hash, length);
			for (HashEntry<K, V> e = this.entries[i]; e != null; e = e.next)
			{
				Object k;
				if (e.hash == hash && ((k = e.key) == key || key.equals(k)))
				{
					e.value = value;
					continue outer;
				}
			}
			
			this.entries[i] = new HashEntry(key, value, hash);
		}
	}
	
	public static int hash(Object key)
	{
		int h = key == null ? 0 : key.hashCode();
		h ^= h >>> 20 ^ h >>> 12;
		return h ^ h >>> 7 ^ h >>> 4;
	}
	
	public static int index(int h, int length)
	{
		return h & length - 1;
	}
	
	public static int grow(int size)
	{
		return (int) ((size + 1) * GROWTH_FACTOR);
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
		return new EntryIterator<Entry<K, V>>()
		{
			@Override
			public HashEntry<K, V> next()
			{
				return this.nextEntry();
			}
			
			@Override
			public String toString()
			{
				return "EntryIterator(" + AbstractHashMap.this + ")";
			}
		};
	}
	
	@Override
	public Iterator<K> keyIterator()
	{
		return new EntryIterator<K>()
		{
			@Override
			public K next()
			{
				return this.nextEntry().key;
			}
			
			@Override
			public String toString()
			{
				return "KeyIterator(" + AbstractHashMap.this + ")";
			}
		};
	}
	
	@Override
	public Iterator<V> valueIterator()
	{
		return new EntryIterator<V>()
		{
			@Override
			public V next()
			{
				return this.nextEntry().value;
			}
			
			@Override
			public String toString()
			{
				return "ValueIterator(" + AbstractHashMap.this + ")";
			}
		};
	}
	
	@Override
	public void forEach(Consumer<? super Entry<K, V>> action)
	{
		for (HashEntry<K, V> e : this.entries)
		{
			while (e != null)
			{
				action.accept(e);
				e = e.next;
			}
		}
	}
	
	@Override
	public void forEach(BiConsumer<? super K, ? super V> action)
	{
		for (HashEntry<K, V> e : this.entries)
		{
			while (e != null)
			{
				action.accept(e.key, e.value);
				e = e.next;
			}
		}
	}
	
	@Override
	public boolean containsKey(Object key)
	{
		return this.get(key) != null;
	}
	
	@Override
	public boolean containsValue(Object value)
	{
		for (HashEntry<K, V> e : this.entries)
		{
			while (e != null)
			{
				if (e.value.equals(value))
				{
					return true;
				}
				e = e.next;
			}
		}
		return false;
	}
	
	@Override
	public boolean contains(Object key, Object value)
	{
		HashEntry<K, V> entry = this.getEntry(key);
		return entry == null ? false : Objects.equals(entry.value, value);
	}
	
	protected HashEntry<K, V> getEntry(Object key)
	{
		if (key == null)
		{
			for (HashEntry<K, V> e = this.entries[0]; e != null; e = e.next)
			{
				if (e.key == null)
				{
					return e;
				}
			}
			return null;
		}
		
		int hash = hash(key);
		for (HashEntry<K, V> e = this.entries[index(hash, this.entries.length)]; e != null; e = e.next)
		{
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key.equals(k)))
			{
				return e;
			}
		}
		return null;
	}
	
	@Override
	public V get(Object key)
	{
		HashEntry<K, V> entry = this.getEntry(key);
		return entry == null ? null : entry.value;
	}
	
	@Override
	public String toString()
	{
		if (this.size == 0)
		{
			return "[]";
		}
		
		StringBuilder buf = new StringBuilder("[ ");
		for (HashEntry<K, V> e : this.entries)
		{
			while (e != null)
			{
				buf.append(e.key).append(" -> ").append(e.value);
				e = e.next;
				
				buf.append(", ");
			}
		}
		int len = buf.length();
		return buf.replace(len - 2, len, " ]").toString();
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
