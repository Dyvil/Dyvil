package dyvil.collection.mutable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import dyvil.collection.immutable.ImmutableMap;
import dyvil.lang.Map;
import dyvil.lang.tuple.Tuple2;

public class HashMap<K, V> implements MutableMap<K, V>
{
	private static final class Entry<K, V> implements Map.Entry<K, V>
	{
		K		key;
		V		value;
		int		hash;
		Entry	next;
		
		Entry(K key, V value, int hash, Entry next)
		{
			super();
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
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + (this.key == null ? 0 : this.key.hashCode());
			result = prime * result + (this.value == null ? 0 : this.value.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (this.getClass() != obj.getClass())
			{
				return false;
			}
			Entry other = (Entry) obj;
			if (this.key == null)
			{
				if (other.key != null)
				{
					return false;
				}
			}
			else if (!this.key.equals(other.key))
			{
				return false;
			}
			if (this.value == null)
			{
				if (other.value != null)
				{
					return false;
				}
			}
			else if (!this.value.equals(other.value))
			{
				return false;
			}
			return true;
		}
		
		@Override
		public String toString()
		{
			return this.key + " -> " + this.value;
		}
	}
	
	private abstract class EntryIterator
	{
		Entry<K, V>	next;		// next entry to return
		Entry<K, V>	current;	// current entry
		int			index;		// current slot
								
		EntryIterator()
		{
			Entry<K, V>[] t = HashMap.this.entries;
			this.current = this.next = null;
			this.index = 0;
			// advance to first entry
			if (t != null && HashMap.this.size > 0)
			{
				do
				{
				}
				while (this.index < t.length && (this.next = t[this.index++]) == null);
			}
		}
		
		public final boolean hasNext()
		{
			return this.next != null;
		}
		
		final Entry<K, V> nextEntry()
		{
			Entry<K, V>[] t;
			Entry<K, V> e = this.next;
			if (e == null)
			{
				throw new NoSuchElementException();
			}
			if ((this.next = (this.current = e).next) == null && (t = HashMap.this.entries) != null)
			{
				do
				{
				}
				while (this.index < t.length && (this.next = t[this.index++]) == null);
			}
			return e;
		}
	}
	
	private static final int	DEFAULT_SIZE		= 16;
	private static final float	DEFAULT_LOAD_FACTOR	= 0.75F;
	private static final int	MAX_ARRAY_SIZE		= Integer.MAX_VALUE - 8;
	
	private int					size;
	private float				loadFactor;
	private int					threshold;
	private Entry[]				entries;
	
	public HashMap()
	{
		this(DEFAULT_SIZE, DEFAULT_LOAD_FACTOR);
	}
	
	public HashMap(int size)
	{
		this(size, DEFAULT_LOAD_FACTOR);
	}
	
	public HashMap(float loadFactor)
	{
		this(DEFAULT_SIZE, loadFactor);
	}
	
	public HashMap(int size, float loadFactor)
	{
		if (size < 0)
		{
			throw new IllegalArgumentException("Invalid Capacity: " + size);
		}
		if (loadFactor <= 0 || Float.isNaN(loadFactor))
		{
			throw new IllegalArgumentException("Invalid Load Factor: " + loadFactor);
		}
		
		this.loadFactor = loadFactor;
		this.entries = new Entry[size];
		this.threshold = (int) Math.min(size * loadFactor, MAX_ARRAY_SIZE + 1);
	}
	
	static int hash(int h)
	{
		h ^= h >>> 20 ^ h >>> 12;
		return h ^ h >>> 7 ^ h >>> 4;
	}
	
	static int index(int h, int length)
	{
		return h & length - 1;
	}
	
	protected void rehash()
	{
		Entry<?, ?>[] oldMap = this.entries;
		int oldCapacity = oldMap.length;
		
		// overflow-conscious code
		int newCapacity = (oldCapacity << 1) + 1;
		if (newCapacity - MAX_ARRAY_SIZE > 0)
		{
			if (oldCapacity == MAX_ARRAY_SIZE)
			{
				// Keep running with MAX_ARRAY_SIZE buckets
				return;
			}
			newCapacity = MAX_ARRAY_SIZE;
		}
		Entry<?, ?>[] newMap = new Entry<?, ?>[newCapacity];
		
		this.threshold = (int) Math.min(newCapacity * this.loadFactor, MAX_ARRAY_SIZE + 1);
		this.entries = newMap;
		
		for (int i = oldCapacity; i-- > 0;)
		{
			Entry e = oldMap[i];
			while (e != null)
			{
				int index = (e.hash & 0x7FFFFFFF) % newCapacity;
				e.next = newMap[index];
				newMap[index] = e;
				e = e.next;
			}
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
	public Iterator<Tuple2<K, V>> iterator()
	{
		class TupleIterator extends EntryIterator implements Iterator<Tuple2<K, V>>
		{
			@Override
			public Tuple2<K, V> next()
			{
				Entry<K, V> entry = this.nextEntry();
				return new Tuple2<K, V>(entry.key, entry.value);
			}
		}
		
		return new TupleIterator();
	}
	
	@Override
	public Iterator<K> keyIterator()
	{
		class KeyIterator extends EntryIterator implements Iterator<K>
		{
			@Override
			public K next()
			{
				return this.nextEntry().key;
			};
		}
		
		return new KeyIterator();
	}
	
	@Override
	public Iterator<V> valueIterator()
	{
		class ValueIterator extends EntryIterator implements Iterator<V>
		{
			@Override
			public V next()
			{
				return this.nextEntry().value;
			};
		}
		
		return new ValueIterator();
	}
	
	@Override
	public Iterator<Map.Entry<K, V>> entryIterator()
	{
		class EntryIteratorImpl extends EntryIterator implements Iterator<Map.Entry<K, V>>
		{
			@Override
			public Map.Entry<K, V> next()
			{
				return this.nextEntry();
			}
		}
		
		return new EntryIteratorImpl();
	}
	
	@Override
	public void forEach(Consumer<? super Tuple2<K, V>> action)
	{
		for (Entry<K, V> e : this.entries)
		{
			while (e != null)
			{
				action.accept(new Tuple2(e.key, e.value));
				e = e.next;
			}
		}
	}
	
	@Override
	public void forEach(BiConsumer<? super K, ? super V> action)
	{
		for (Entry<K, V> e : this.entries)
		{
			while (e != null)
			{
				action.accept(e.key, e.value);
				e = e.next;
			}
		}
	}
	
	@Override
	public boolean $qmark(Object key)
	{
		return this.apply((K) key) != null;
	}
	
	@Override
	public boolean $qmark(Object key, Object value)
	{
		return value.equals(this.apply((K) key));
	}
	
	@Override
	public boolean $qmark$colon(V value)
	{
		for (Entry<K, V> e : this.entries)
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
	
	private V applyNull()
	{
		for (Entry<K, V> e = this.entries[0]; e != null; e = e.next)
		{
			if (e.key == null)
			{
				return e.value;
			}
		}
		return null;
	}
	
	@Override
	public V apply(K key)
	{
		if (key == null)
		{
			return this.applyNull();
		}
		
		int hash = hash(key.hashCode());
		for (Entry<K, V> e = this.entries[index(hash, this.entries.length)]; e != null; e = e.next)
		{
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key.equals(k)))
			{
				return e.value;
			}
		}
		return null;
	}
	
	@Override
	public MutableMap<K, V> $plus(K key, V value)
	{
		HashMap<K, V> copy = this.copy();
		copy.update(key, value);
		return copy;
	}
	
	@Override
	public MutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map)
	{
		HashMap<K, V> copy = this.copy();
		copy.$plus$plus$eq(map);
		return copy;
	}
	
	@Override
	public MutableMap<K, V> $minus(K key)
	{
		HashMap<K, V> copy = this.copy();
		copy.$minus$eq(key);
		return copy;
	}
	
	@Override
	public MutableMap<K, V> $minus(K key, V value)
	{
		HashMap<K, V> copy = this.copy();
		copy.$minus(key);
		return copy;
	}
	
	@Override
	public MutableMap<K, V> $minus$colon(V value)
	{
		HashMap<K, V> copy = this.copy();
		copy.$minus$colon(value);
		return copy;
	}
	
	@Override
	public MutableMap<K, V> $minus$minus(Map<? extends K, ? extends V> map)
	{
		HashMap<K, V> copy = this.copy();
		copy.$minus$minus(map);
		return copy;
	}
	
	@Override
	public <U> MutableMap<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper)
	{
		HashMap<K, U> copy = (HashMap<K, U>) this.copy();
		copy.map((BiFunction<? super K, ? super U, ? extends U>) mapper);
		return copy;
	}
	
	@Override
	public MutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		HashMap<K, V> copy = this.copy();
		copy.filter(condition);
		return copy;
	}
	
	@Override
	public void clear()
	{
		this.size = 0;
		int length = this.entries.length;
		for (int i = 0; i < length; i++)
		{
			this.entries[i] = null;
		}
	}
	
	private void addEntry(int hash, K key, V value, int index)
	{
		Entry<?, ?> tab[] = this.entries;
		if (this.size >= this.threshold)
		{
			// Rehash the table if the threshold is exceeded
			this.rehash();
			
			tab = this.entries;
			hash = key.hashCode();
			index = (hash & 0x7FFFFFFF) % tab.length;
		}
		
		// Creates the new entry.
		@SuppressWarnings("unchecked")
		Entry<K, V> e = (Entry<K, V>) tab[index];
		tab[index] = new Entry(key, value, hash, e);
		this.size++;
	}
	
	private V putNull(V value)
	{
		for (Entry<K, V> e = this.entries[0]; e != null; e = e.next)
		{
			if (e.key == null)
			{
				V old = e.value;
				e.value = value;
				return old;
			}
		}
		this.addEntry(0, null, value, 0);
		return null;
	}
	
	@Override
	public void update(K key, V value)
	{
		if (key == null)
		{
			this.putNull(value);
			return;
		}
		
		int hash = hash(key.hashCode());
		int i = index(hash, this.entries.length);
		for (Entry<K, V> e = this.entries[i]; e != null; e = e.next)
		{
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key.equals(k)))
			{
				e.value = value;
				return;
			}
		}
		
		this.addEntry(hash, key, value, i);
	}
	
	@Override
	public V put(K key, V value)
	{
		if (key == null)
		{
			return this.putNull(value);
		}
		
		int hash = hash(key.hashCode());
		int i = index(hash, this.entries.length);
		for (Entry<K, V> e = this.entries[i]; e != null; e = e.next)
		{
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key.equals(k)))
			{
				V oldValue = e.value;
				e.value = value;
				return oldValue;
			}
		}
		
		this.addEntry(hash, key, value, i);
		return null;
	}
	
	@Override
	public void $plus$plus$eq(Map<? extends K, ? extends V> map)
	{
		Iterator<?> iterator = map.entryIterator();
		while (iterator.hasNext())
		{
			Map.Entry<? extends K, ? extends V> entry = (dyvil.lang.Map.Entry<? extends K, ? extends V>) iterator.next();
			this.update(entry.getKey(), entry.getValue());
		}
	}
	
	private V removeNull()
	{
		Entry<K, V> prev = this.entries[0];
		Entry<K, V> e = prev;
		
		while (e != null)
		{
			Entry<K, V> next = e.next;
			if (e.hash == 0 && e.key == null)
			{
				this.size--;
				if (prev == e)
				{
					this.entries[0] = next;
				}
				else
				{
					prev.next = next;
				}
				return e.value;
			}
			prev = e;
			e = next;
		}
		
		return null;
	}
	
	@Override
	public void $minus$eq(K key)
	{
		if (key == null)
		{
			this.removeNull();
			return;
		}
		
		int hash = hash(key.hashCode());
		int i = index(hash, this.entries.length);
		Entry<K, V> prev = this.entries[i];
		Entry<K, V> e = prev;
		
		while (e != null)
		{
			Entry<K, V> next = e.next;
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key.equals(k)))
			{
				this.size--;
				if (prev == e)
				{
					this.entries[i] = next;
				}
				else
				{
					prev.next = next;
				}
			}
			prev = e;
			e = next;
		}
	}
	
	@Override
	public V remove(K key)
	{
		if (key == null)
		{
			return this.removeNull();
		}
		
		int hash = hash(key.hashCode());
		int i = index(hash, this.entries.length);
		Entry<K, V> prev = this.entries[i];
		Entry<K, V> e = prev;
		
		while (e != null)
		{
			Entry<K, V> next = e.next;
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key.equals(k)))
			{
				this.size--;
				if (prev == e)
				{
					this.entries[i] = next;
				}
				else
				{
					prev.next = next;
				}
				
				return e.value;
			}
			prev = e;
			e = next;
		}
		
		return null;
	}
	
	@Override
	public boolean remove(K key, V value)
	{
		int hash = key == null ? 0 : hash(key.hashCode());
		int i = index(hash, this.entries.length);
		Entry<K, V> prev = this.entries[i];
		Entry<K, V> e = prev;
		
		while (e != null)
		{
			Entry<K, V> next = e.next;
			if (e.hash == hash)
			{
				Object k = e.key;
				Object v = e.value;
				if ((k == key || key.equals(k)) && (v == value || value != null && value.equals(v)))
				{
					this.size--;
					if (prev == e)
					{
						this.entries[i] = next;
					}
					else
					{
						prev.next = next;
					}
					
					return true;
				}
			}
			prev = e;
			e = next;
		}
		
		return false;
	}
	
	@Override
	public void $minus$colon$eq(V value)
	{
		for (int i = 0; i < this.entries.length; i++)
		{
			Entry<K, V> prev = this.entries[i];
			Entry<K, V> e = prev;
			
			while (e != null)
			{
				Entry<K, V> next = e.next;
				Object v = e.value;
				if (v == value || value != null && value.equals(v))
				{
					this.size--;
					if (prev == e)
					{
						this.entries[i] = next;
					}
					else
					{
						prev.next = next;
					}
					
					return;
				}
				prev = e;
				e = next;
			}
		}
	}
	
	@Override
	public void $minus$minus$eq(Map<? extends K, ? extends V> map)
	{
		Iterator<?> iterator = map.entryIterator();
		while (iterator.hasNext())
		{
			Map.Entry<? extends K, ? extends V> entry = (dyvil.lang.Map.Entry<? extends K, ? extends V>) iterator.next();
			this.remove(entry.getKey(), entry.getValue());
		}
	}
	
	@Override
	public void map(BiFunction<? super K, ? super V, ? extends V> mapper)
	{
		for (Entry<K, V> entry : this.entries)
		{
			while (entry != null)
			{
				entry.value = mapper.apply(entry.key, entry.value);
				entry = entry.next;
			}
		}
	}
	
	@Override
	public void filter(BiPredicate<? super K, ? super V> condition)
	{
		for (int i = 0; i < this.entries.length; i++)
		{
			Entry<K, V> prev = this.entries[i];
			Entry<K, V> e = prev;
			
			while (e != null)
			{
				Entry<K, V> next = e.next;
				if (!condition.test(e.key, e.value))
				{
					this.size--;
					if (prev == e)
					{
						this.entries[i] = next;
					}
					else
					{
						prev.next = next;
					}
					
					return;
				}
				prev = e;
				e = next;
			}
		}
	}
	
	@Override
	public HashMap<K, V> copy()
	{
		return null;
	}
	
	@Override
	public ImmutableMap<K, V> immutable()
	{
		return null; // FIXME
	}
	
	@Override
	public String toString()
	{
		if (this.size == 0)
		{
			return "[]";
		}
		
		StringBuilder buf = new StringBuilder("[ ");
		for (Entry<K, V> e : this.entries)
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
}
