package dyvil.collection.impl;

import dyvil.collection.Entry;
import dyvil.collection.ImmutableMap;
import dyvil.collection.Map;
import dyvil.collection.MutableMap;
import dyvil.math.MathUtils;
import dyvil.util.None;
import dyvil.util.Option;
import dyvil.util.Some;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class AbstractHashMap<K, V> implements Map<K, V>
{
	protected static final class HashEntry<K, V> implements Entry<K, V>
	{
		private static final long serialVersionUID = 6421167357975687099L;
		
		public transient K               key;
		public transient V               value;
		public transient int             hash;
		public transient HashEntry<K, V> next;
		
		public HashEntry(K key, V value, int hash)
		{
			this.key = key;
			this.value = value;
			this.hash = hash;
		}
		
		public HashEntry(K key, V value, int hash, HashEntry<K, V> next)
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
		
		private void writeObject(java.io.ObjectOutputStream out) throws IOException
		{
			out.defaultWriteObject();
			
			out.writeObject(this.key);
			out.writeObject(this.value);
			out.writeObject(this.next);
		}
		
		private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
		{
			in.defaultReadObject();
			
			this.key = (K) in.readObject();
			this.value = (V) in.readObject();
			this.next = (HashEntry<K, V>) in.readObject();
			this.hash = hash(this.key);
		}
	}
	
	protected abstract class EntryIterator<E> implements Iterator<E>
	{
		HashEntry<K, V> next;        // next entry to return
		HashEntry<K, V> current;    // current entry
		int             index;        // current slot
		
		EntryIterator()
		{
			HashEntry<K, V>[] t = AbstractHashMap.this.entries;
			this.current = this.next = null;
			this.index = 0;
			// advance to first entry
			if (t != null && AbstractHashMap.this.size > 0)
			{
				this.advance(t);
			}
		}

		private void advance(HashEntry<K, V>[] t)
		{
			while (true)
			{
				if (!(this.index < t.length && (this.next = t[this.index++]) == null))
				{
					break;
				}
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
				this.advance(t);
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
			
			AbstractHashMap.this.removeEntry(e);
			this.current = null;
		}
	}
	
	private static final long serialVersionUID = 408161126967974108L;
	
	public static final float GROWTH_FACTOR       = 1.125F;
	public static final int   DEFAULT_CAPACITY    = 16;
	public static final float DEFAULT_LOAD_FACTOR = 0.75F;
	public static final int   MAX_ARRAY_SIZE      = Integer.MAX_VALUE - 8;
	
	protected transient int               size;
	protected transient HashEntry<K, V>[] entries;
	
	public AbstractHashMap()
	{
	}
	
	public AbstractHashMap(int capacity)
	{
		if (capacity < 0)
		{
			throw new IllegalArgumentException("Invalid Capacity: " + capacity);
		}
		this.entries = (HashEntry<K, V>[]) new HashEntry[MathUtils.powerOfTwo(grow(capacity))];
	}
	
	public AbstractHashMap(Map<K, V> map)
	{
		int size = map.size();
		int length = MathUtils.powerOfTwo(grow(size));
		HashEntry<K, V>[] entries = this.entries = (HashEntry<K, V>[]) new HashEntry[length];
		this.size = size;
		
		// Assume unique elements
		for (Entry<K, V> entry : map)
		{
			K key = entry.getKey();
			int hash = hash(key);
			int i = index(hash, length);
			entries[i] = new HashEntry<>(key, entry.getValue(), hash, entries[i]);
		}
	}
	
	public AbstractHashMap(AbstractHashMap<K, V> map)
	{
		int size = this.size = map.size;
		int length = MathUtils.powerOfTwo(AbstractHashMap.grow(size));
		HashEntry<K, V>[] newEntries = this.entries = (HashEntry<K, V>[]) new HashEntry[length];
		
		for (HashEntry<K, V> e : map.entries)
		{
			while (e != null)
			{
				int index = index(e.hash, length);
				HashEntry<K, V> newEntry = new HashEntry<>(e.key, e.value, e.hash);
				if (newEntries[index] != null)
				{
					newEntry.next = newEntries[index];
				}
				
				newEntries[index] = newEntry;
				e = e.next;
			}
		}
	}
	
	@SafeVarargs
	public AbstractHashMap(Entry<K, V>... entries)
	{
		int length = MathUtils.powerOfTwo(grow(entries.length));
		int size = 0;
		HashEntry<K, V>[] hashTable = this.entries = (HashEntry<K, V>[]) new HashEntry[length];
		
		outer:
		for (Entry<K, V> entry : entries)
		{
			K key = entry.getKey();
			V value = entry.getValue();
			
			int hash = hash(key);
			int i = index(hash, length);
			for (HashEntry e = hashTable[i]; e != null; e = e.next)
			{
				Object k;
				if (e.hash == hash && ((k = e.key) == key || key != null && key.equals(k)))
				{
					e.value = value;
					continue outer;
				}
			}
			
			hashTable[i] = new HashEntry<>(key, value, hash, hashTable[i]);
			size++;
		}
		
		this.size = size;
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
	
	protected void flatten()
	{
		this.ensureCapacityInternal(this.entries.length << 1);
	}
	
	public void ensureCapacity(int newCapacity)
	{
		if (newCapacity > this.entries.length)
		{
			this.ensureCapacityInternal(MathUtils.powerOfTwo(newCapacity));
		}
	}
	
	protected void ensureCapacityInternal(int newCapacity)
	{
		HashEntry[] oldMap = this.entries;
		int oldCapacity = oldMap.length;
		
		// overflow-conscious code
		if (newCapacity - MAX_ARRAY_SIZE > 0)
		{
			if (oldCapacity == MAX_ARRAY_SIZE)
			{
				// Keep running with MAX_ARRAY_SIZE buckets
				return;
			}
			newCapacity = MAX_ARRAY_SIZE;
		}
		
		HashEntry[] newMap = this.entries = (HashEntry<K, V>[]) new HashEntry[newCapacity];
		for (int i = oldCapacity; i-- > 0; )
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
		
		this.updateThreshold(newCapacity);
	}
	
	protected void updateThreshold(int newCapacity)
	{
	}
	
	protected void putInternal(Map<? extends K, ? extends V> map)
	{
		this.ensureCapacity(this.size + map.size());
		
		for (Entry<? extends K, ? extends V> entry : map)
		{
			this.putInternal(entry.getKey(), entry.getValue());
		}
	}
	
	protected void putInternal(K key, V value)
	{
		int hash = hash(key);
		int i = index(hash, this.entries.length);
		for (HashEntry<K, V> e = this.entries[i]; e != null; e = e.next)
		{
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key != null && key.equals(k)))
			{
				e.value = value;
				return;
			}
		}
		
		this.addEntry(hash, key, value, i);
	}
	
	protected abstract void addEntry(int hash, K key, V value, int index);
	
	protected void removeEntry(HashEntry<K, V> entry)
	{
		AbstractHashMap.this.size--;
		int index = index(entry.hash, AbstractHashMap.this.entries.length);
		HashEntry<K, V> e = AbstractHashMap.this.entries[index];
		if (e == entry)
		{
			AbstractHashMap.this.entries[index] = entry.next;
		}
		else
		{
			HashEntry<K, V> prev;
			do
			{
				prev = e;
				e = e.next;
			}
			while (e != entry);
			
			prev.next = entry.next;
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
		return entry != null && Objects.equals(entry.value, value);
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
	public Option<V> getOption(Object key)
	{
		HashEntry<K, V> entry = this.getEntry(key);
		return entry == null ? None.instance : new Some<>(entry.value);
	}

	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy()
	{
		return new dyvil.collection.mutable.HashMap<>();
	}

	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy(int capacity)
	{
		return new dyvil.collection.mutable.HashMap<>(capacity);
	}

	@Override
	public MutableMap<K, V> mutableCopy()
	{
		return new dyvil.collection.mutable.HashMap<>(this);
	}

	@Override
	public ImmutableMap<K, V> immutableCopy()
	{
		return new dyvil.collection.immutable.HashMap<>(this);
	}

	@Override
	public <RK, RV> ImmutableMap.Builder<RK, RV> immutableBuilder()
	{
		return dyvil.collection.immutable.HashMap.builder();
	}

	@Override
	public <RK, RV> ImmutableMap.Builder<RK, RV> immutableBuilder(int capacity)
	{
		return dyvil.collection.immutable.HashMap.builder(capacity);
	}
	
	@Override
	public java.util.Map<K, V> toJava()
	{
		java.util.HashMap<K, V> map = new java.util.HashMap<>(this.size);
		for (Entry<K, V> entry : this)
		{
			map.put(entry.getKey(), entry.getValue());
		}
		return map;
	}
	
	@Override
	public String toString()
	{
		if (this.size == 0)
		{
			return Map.EMPTY_STRING;
		}
		
		final StringBuilder builder = new StringBuilder(Map.START_STRING);

		for (HashEntry<K, V> entry : this.entries)
		{
			for (; entry != null; entry = entry.next)
			{
				builder.append(entry.key).append(Map.KEY_VALUE_SEPARATOR_STRING).append(entry.value)
				       .append(Map.ENTRY_SEPARATOR_STRING);
			}
		}

		final int len = builder.length();
		return builder.replace(len - Map.ENTRY_SEPARATOR_STRING.length(), len, Map.END_STRING).toString();
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
		
		int len = this.entries.length;
		
		out.writeInt(this.size);
		out.writeInt(len);
		
		// Write key-value pairs, sequentially
		for (HashEntry<K, V> entry : this.entries)
		{
			for (HashEntry<K, V> subEntry = entry; subEntry != null; subEntry = subEntry.next)
			{
				out.writeObject(subEntry.key);
				out.writeObject(subEntry.value);
			}
		}
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		
		this.size = in.readInt();
		int len = in.readInt();
		
		this.entries = (HashEntry<K, V>[]) new HashEntry[len];
		for (int i = 0; i < len; i++)
		{
			this.putInternal((K) in.readObject(), (V) in.readObject());
		}
	}
}
