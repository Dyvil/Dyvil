package dyvil.collection.mutable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import dyvil.lang.Entry;
import dyvil.lang.Map;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import dyvil.collection.ImmutableMap;
import dyvil.collection.MutableMap;
import dyvil.collection.immutable.ArrayMap;
import dyvil.math.MathUtils;
import dyvil.tuple.Tuple2;

@NilConvertible
@ArrayConvertible
public class HashMap<K, V> implements MutableMap<K, V>
{
	private static final class HashEntry<K, V> implements Entry<K, V>
	{
		K			key;
		V			value;
		int			hash;
		HashEntry	next;
		
		HashEntry(K key, V value, int hash)
		{
			this.key = key;
			this.value = value;
			this.hash = hash;
		}
		
		HashEntry(K key, V value, int hash, HashEntry next)
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
	
	private abstract class EntryIterator<E> implements Iterator<E>
	{
		HashEntry<K, V>	next;		// next entry to return
		HashEntry<K, V>	current;	// current entry
		int				index;		// current slot
									
		EntryIterator()
		{
			HashEntry<K, V>[] t = HashMap.this.entries;
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
			if ((this.next = (this.current = e).next) == null && (t = HashMap.this.entries) != null)
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
			
			HashMap.this.size--;
			this.current = null;
			int index = index(e.hash, HashMap.this.entries.length);
			HashEntry<K, V> entry = HashMap.this.entries[index];
			if (entry == e)
			{
				HashMap.this.entries[index] = e.next;
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
	
	static final float	GROWTH_FACTOR		= 1.0625F;
	static final int	DEFAULT_CAPACITY	= 16;
	static final float	DEFAULT_LOAD_FACTOR	= 0.75F;
	static final int	MAX_ARRAY_SIZE		= Integer.MAX_VALUE - 8;
	
	private int			size;
	private float		loadFactor;
	private int			threshold;
	private HashEntry[]	entries;
	
	public static <K, V> HashMap<K, V> apply()
	{
		return new HashMap();
	}
	
	HashMap(int size, float loadFactor, HashEntry[] entries)
	{
		this.size = size;
		this.loadFactor = loadFactor;
		this.threshold = (int) ((size << 1) / loadFactor);
		this.entries = entries;
	}
	
	public HashMap()
	{
		this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
	}
	
	public HashMap(int size)
	{
		this(size, DEFAULT_LOAD_FACTOR);
	}
	
	public HashMap(float loadFactor)
	{
		this(DEFAULT_CAPACITY, loadFactor);
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
		this.entries = new HashEntry[MathUtils.powerOfTwo(size)];
		this.threshold = (int) Math.min(size * loadFactor, MAX_ARRAY_SIZE + 1);
	}
	
	public HashMap(Map<K, V> map)
	{
		this(grow(map.size()), DEFAULT_LOAD_FACTOR);
		for (Entry<K, V> entry : map)
		{
			this.subscript_$eq(entry.getKey(), entry.getValue());
		}
	}
	
	static int hash(Object key)
	{
		int h = key == null ? 0 : key.hashCode();
		h ^= h >>> 20 ^ h >>> 12;
		return h ^ h >>> 7 ^ h >>> 4;
	}
	
	static int index(int h, int length)
	{
		return h & length - 1;
	}
	
	static int grow(int size)
	{
		return (int) ((size + 1) * GROWTH_FACTOR);
	}
	
	protected void rehash()
	{
		HashEntry[] oldMap = this.entries;
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
		HashEntry[] newMap = new HashEntry[newCapacity];
		
		this.threshold = (int) Math.min(newCapacity * this.loadFactor, MAX_ARRAY_SIZE + 1);
		this.entries = newMap;
		
		for (int i = oldCapacity; i-- > 0;)
		{
			HashEntry e = oldMap[i];
			while (e != null)
			{
				int index = index(e.hash, newCapacity);
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
				return "EntryIterator(" + HashMap.this + ")";
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
				return "KeyIterator(" + HashMap.this + ")";
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
				return "ValueIterator(" + HashMap.this + ")";
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
				action.accept(new Tuple2(e.key, e.value));
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
	public boolean contains(Object key, Object value)
	{
		return value.equals(this.get(key));
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
	public V get(Object key)
	{
		if (key == null)
		{
			for (HashEntry<K, V> e = this.entries[0]; e != null; e = e.next)
			{
				if (e.key == null)
				{
					return e.value;
				}
			}
			return null;
		}
		
		int hash = hash(key.hashCode());
		for (HashEntry<K, V> e = this.entries[index(hash, this.entries.length)]; e != null; e = e.next)
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
		copy.subscript_$eq(key, value);
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
	public MutableMap<K, V> $minus(Object key)
	{
		HashMap<K, V> copy = this.copy();
		copy.$minus$eq(key);
		return copy;
	}
	
	@Override
	public MutableMap<K, V> $minus(Object key, Object value)
	{
		HashMap<K, V> copy = this.copy();
		copy.$minus(key);
		return copy;
	}
	
	@Override
	public MutableMap<K, V> $minus$colon(Object value)
	{
		HashMap<K, V> copy = this.copy();
		copy.$minus$colon(value);
		return copy;
	}
	
	@Override
	public MutableMap<K, V> $minus$minus(Map<? super K, ? super V> map)
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
		HashEntry[] tab = this.entries;
		if (this.size >= this.threshold)
		{
			// Rehash the table if the threshold is exceeded
			this.rehash();
			
			tab = this.entries;
			hash = hash(key);
			index = index(hash, tab.length);
		}
		
		tab[index] = new HashEntry(key, value, hash, tab[index]);
		this.size++;
	}
	
	private V putNull(V value)
	{
		for (HashEntry<K, V> e = this.entries[0]; e != null; e = e.next)
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
	public void subscript_$eq(K key, V value)
	{
		if (key == null)
		{
			this.putNull(value);
			return;
		}
		
		int hash = hash(key);
		int i = index(hash, this.entries.length);
		for (HashEntry<K, V> e = this.entries[i]; e != null; e = e.next)
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
		
		int hash = hash(key);
		int i = index(hash, this.entries.length);
		for (HashEntry<K, V> e = this.entries[i]; e != null; e = e.next)
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
	
	private V removeNull()
	{
		HashEntry<K, V> prev = this.entries[0];
		HashEntry<K, V> e = prev;
		
		while (e != null)
		{
			HashEntry<K, V> next = e.next;
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
	public V removeKey(Object key)
	{
		if (key == null)
		{
			return this.removeNull();
		}
		
		int hash = hash(key);
		int i = index(hash, this.entries.length);
		HashEntry<K, V> prev = this.entries[i];
		HashEntry<K, V> e = prev;
		
		while (e != null)
		{
			HashEntry<K, V> next = e.next;
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
	public boolean removeValue(Object value)
	{
		for (int i = 0; i < this.entries.length; i++)
		{
			HashEntry<K, V> prev = this.entries[i];
			HashEntry<K, V> e = prev;
			
			while (e != null)
			{
				HashEntry<K, V> next = e.next;
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
					
					return true;
				}
				prev = e;
				e = next;
			}
		}
		
		return false;
	}
	
	@Override
	public void map(BiFunction<? super K, ? super V, ? extends V> mapper)
	{
		for (HashEntry<K, V> entry : this.entries)
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
			HashEntry<K, V> prev = this.entries[i];
			HashEntry<K, V> e = prev;
			
			while (e != null)
			{
				HashEntry<K, V> next = e.next;
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
				}
				prev = e;
				e = next;
			}
		}
	}
	
	@Override
	public HashMap<K, V> copy()
	{
		int len = MathUtils.powerOfTwo(grow(this.size));
		HashEntry[] newEntries = new HashEntry[len];
		for (HashEntry<K, V> e : this.entries)
		{
			while (e != null)
			{
				int index = index(e.hash, len);
				HashEntry<K, V> newEntry = new HashEntry(e.key, e.value, e.hash);
				if (newEntries[index] != null)
				{
					newEntry.next = newEntries[index];
				}
				
				newEntries[index] = newEntry;
				e = e.next;
			}
		}
		
		return new HashMap<K, V>(this.size, this.loadFactor, newEntries);
	}
	
	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy()
	{
		return new HashMap(this.size, this.loadFactor);
	}
	
	@Override
	public ImmutableMap<K, V> immutable()
	{
		return new ArrayMap(this); // TODO immutable.HashMap
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
