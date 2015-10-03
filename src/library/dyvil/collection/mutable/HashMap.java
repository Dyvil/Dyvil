package dyvil.collection.mutable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import dyvil.collection.ImmutableMap;
import dyvil.collection.Map;
import dyvil.collection.MutableMap;
import dyvil.collection.impl.AbstractHashMap;
import dyvil.math.MathUtils;
import dyvil.tuple.Tuple2;

@NilConvertible
@ArrayConvertible
public class HashMap<K, V> extends AbstractHashMap<K, V>implements MutableMap<K, V>
{
	private float	loadFactor;
	private int		threshold;
	
	public static <K, V> HashMap<K, V> apply()
	{
		return new HashMap();
	}
	
	public static <K, V> HashMap<K, V> apply(Tuple2<K, V>[] tuples)
	{
		return new HashMap(tuples);
	}
	
	public HashMap()
	{
		this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
	}
	
	public HashMap(int capacity)
	{
		this(capacity, DEFAULT_LOAD_FACTOR);
	}
	
	public HashMap(float loadFactor)
	{
		this(DEFAULT_CAPACITY, loadFactor);
	}
	
	public HashMap(int capacity, float loadFactor)
	{
		if (capacity < 0)
		{
			throw new IllegalArgumentException("Invalid Capacity: " + capacity);
		}
		if (loadFactor <= 0 || Float.isNaN(loadFactor))
		{
			throw new IllegalArgumentException("Invalid Load Factor: " + loadFactor);
		}
		
		this.loadFactor = loadFactor;
		this.entries = new HashEntry[MathUtils.powerOfTwo(capacity)];
		this.threshold = (int) Math.min(capacity * loadFactor, MAX_ARRAY_SIZE + 1);
	}
	
	public HashMap(Map<K, V> map)
	{
		super(map);
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		this.threshold = (int) (this.entries.length * DEFAULT_LOAD_FACTOR);
	}
	
	public HashMap(AbstractHashMap<K, V> map)
	{
		super(map);
	}
	
	public HashMap(Tuple2<K, V>... tuples)
	{
		super(tuples);
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		this.threshold = (int) (this.entries.length * DEFAULT_LOAD_FACTOR);
	}
	
	@Override
	protected void updateThreshold(int newCapacity)
	{
		this.threshold = (int) Math.min(newCapacity * this.loadFactor, MAX_ARRAY_SIZE + 1);
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
	
	@Override
	public void subscript_$eq(K key, V value)
	{
		this.putInternal(key, value);
	}
	
	@Override
	public V put(K key, V value)
	{
		int hash = hash(key);
		int i = index(hash, this.entries.length);
		for (HashEntry<K, V> e = this.entries[i]; e != null; e = e.next)
		{
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key != null && key.equals(k)))
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
	protected void addEntry(int hash, K key, V value, int index)
	{
		HashEntry[] tab = this.entries;
		if (this.size >= this.threshold)
		{
			// Rehash / flatten the table if the threshold is exceeded
			this.flatten();
			
			tab = this.entries;
			hash = hash(key);
			index = index(hash, tab.length);
		}
		
		tab[index] = new HashEntry(key, value, hash, tab[index]);
		this.size++;
	}
	
	@Override
	public void putAll(Map<? extends K, ? extends V> map)
	{
		this.putInternal(map);
	}
	
	@Override
	public boolean putIfAbsent(K key, V value)
	{
		int hash = hash(key);
		int i = index(hash, this.entries.length);
		for (HashEntry<K, V> e = this.entries[i]; e != null; e = e.next)
		{
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key != null && key.equals(k)))
			{
				return false;
			}
		}
		
		this.addEntry(hash, key, value, i);
		return true;
	}
	
	@Override
	public boolean replace(K key, V oldValue, V newValue)
	{
		int hash = hash(key);
		int i = index(hash, this.entries.length);
		for (HashEntry<K, V> e = this.entries[i]; e != null; e = e.next)
		{
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key != null && key.equals(k)))
			{
				if (!Objects.equals(oldValue, newValue))
				{
					return false;
				}
				e.value = newValue;
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public V replace(K key, V newValue)
	{
		int hash = hash(key);
		int i = index(hash, this.entries.length);
		for (HashEntry<K, V> e = this.entries[i]; e != null; e = e.next)
		{
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key != null && key.equals(k)))
			{
				V oldValue = e.value;
				e.value = newValue;
				return oldValue;
			}
		}
		
		return null;
	}
	
	@Override
	public V removeKey(Object key)
	{
		int hash = hash(key);
		int i = index(hash, this.entries.length);
		HashEntry<K, V> prev = this.entries[i];
		HashEntry<K, V> e = prev;
		
		while (e != null)
		{
			HashEntry<K, V> next = e.next;
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key != null && key.equals(k)))
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
	public boolean remove(Object key, Object value)
	{
		int hash = hash(key);
		int i = index(hash, this.entries.length);
		HashEntry<K, V> prev = this.entries[i];
		HashEntry<K, V> e = prev;
		
		while (e != null)
		{
			HashEntry<K, V> next = e.next;
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key != null && key.equals(k)))
			{
				if (!Objects.equals(value, e.value))
				{
					return false;
				}
				
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
		return new HashMap<K, V>(this);
	}
	
	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy()
	{
		return new HashMap(this.size, this.loadFactor);
	}
	
	@Override
	public ImmutableMap<K, V> immutable()
	{
		return new dyvil.collection.immutable.HashMap(this);
	}
}
