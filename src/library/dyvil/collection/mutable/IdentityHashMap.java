package dyvil.collection.mutable;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import dyvil.lang.literal.NilConvertible;

import dyvil.collection.ImmutableMap;
import dyvil.collection.Map;
import dyvil.collection.MutableMap;
import dyvil.collection.immutable.ArrayMap;
import dyvil.collection.impl.AbstractHashMap;
import dyvil.collection.impl.AbstractIdentityHashMap;
import dyvil.tuple.Tuple2;

@NilConvertible
public class IdentityHashMap<K, V> extends AbstractIdentityHashMap<K, V>implements MutableMap<K, V>
{
	private float	loadFactor;
	private int		threshold;
	
	public static <K, V> IdentityHashMap<K, V> apply()
	{
		return new IdentityHashMap();
	}
	
	public static <K, V> IdentityHashMap<K, V> apply(Tuple2<K, V>... entries)
	{
		return new IdentityHashMap(entries);
	}
	
	public IdentityHashMap()
	{
		this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
	}
	
	public IdentityHashMap(int capacity)
	{
		this(capacity, DEFAULT_LOAD_FACTOR);
	}
	
	public IdentityHashMap(float loadFactor)
	{
		this(DEFAULT_CAPACITY, loadFactor);
	}
	
	public IdentityHashMap(int capacity, float loadFactor)
	{
		super(capacity);
		if (loadFactor <= 0 || Float.isNaN(loadFactor))
		{
			throw new IllegalArgumentException("Invalid Load Factor: " + loadFactor);
		}
		
		this.loadFactor = loadFactor;
		this.threshold = (int) Math.min(capacity * loadFactor, AbstractHashMap.MAX_ARRAY_SIZE + 1);
	}
	
	public IdentityHashMap(Map<K, V> map)
	{
		super(map);
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		this.updateThreshold(this.table.length >> 1);
	}
	
	public IdentityHashMap(AbstractIdentityHashMap<K, V> map)
	{
		super(map);
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		this.updateThreshold(this.table.length >> 1);
	}
	
	public IdentityHashMap(Tuple2<K, V>... entries)
	{
		super(entries);
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		this.updateThreshold(this.table.length >> 1);
	}
	
	@Override
	protected void updateThreshold(int newCapacity)
	{
		this.threshold = (int) (newCapacity * this.loadFactor);
	}
	
	@Override
	public void clear()
	{
		this.size = 0;
		Arrays.fill(this.table, null);
	}
	
	@Override
	public V put(K key, V value)
	{
		return this.putInternal(key, value);
	}
	
	@Override
	protected void addEntry(int index, Object key, V value)
	{
		this.table[index] = key;
		this.table[index + 1] = value;
		if (++this.size >= this.threshold)
		{
			this.flatten();
		}
	}
	
	@Override
	public boolean putIfAbsent(K key, V value)
	{
		Object k = maskNull(key);
		Object[] tab = this.table;
		int len = tab.length;
		int i = index(k, len);
		
		Object item;
		while ((item = tab[i]) != null)
		{
			if (item == k)
			{
				return false;
			}
			i = nextKeyIndex(i, len);
		}
		
		this.addEntry(i, k, value);
		return true;
	}
	
	@Override
	public boolean replace(K key, V oldValue, V newValue)
	{
		Object k = maskNull(key);
		Object[] tab = this.table;
		int len = tab.length;
		int i = index(k, len);
		
		Object item;
		while ((item = tab[i]) != null)
		{
			if (item == k)
			{
				if (tab[i + 1] != oldValue)
				{
					return false;
				}
				tab[i + 1] = newValue;
				return true;
			}
			i = nextKeyIndex(i, len);
		}
		
		return false;
	}
	
	@Override
	public V replace(K key, V newValue)
	{
		Object k = maskNull(key);
		Object[] tab = this.table;
		int len = tab.length;
		int i = index(k, len);
		
		Object item;
		while ((item = tab[i]) != null)
		{
			if (item == k)
			{
				V oldValue = (V) tab[i + 1];
				tab[i + 1] = newValue;
				return oldValue;
			}
			i = nextKeyIndex(i, len);
		}
		
		return null;
	}
	
	private void closeDeletion(int index)
	{
		Object[] tab = this.table;
		int len = tab.length;
		
		Object item;
		for (int i = nextKeyIndex(index, len); (item = tab[i]) != null; i = nextKeyIndex(i, len))
		{
			int r = index(item, len);
			if (i < r && (r <= index || index <= i) || r <= index && index <= i)
			{
				tab[index] = item;
				tab[index + 1] = tab[i + 1];
				tab[i] = null;
				tab[i + 1] = null;
				index = i;
			}
		}
	}
	
	@Override
	public V removeKey(Object key)
	{
		Object k = maskNull(key);
		Object[] tab = this.table;
		int len = tab.length;
		int i = index(k, len);
		
		while (true)
		{
			Object item = tab[i];
			if (item == k)
			{
				this.size--;
				V oldValue = (V) tab[i + 1];
				tab[i + 1] = null;
				tab[i] = null;
				this.closeDeletion(i);
				return oldValue;
			}
			if (item == null)
			{
				return null;
			}
			i = nextKeyIndex(i, len);
		}
	}
	
	@Override
	public boolean removeValue(Object value)
	{
		boolean removed = false;
		Object[] tab = this.table;
		for (int i = 1; i < tab.length; i += 2)
		{
			if (tab[i] == value && tab[i - 1] != null)
			{
				tab[i] = tab[i - 1] = null;
				removed = true;
				this.closeDeletion(i);
			}
		}
		return removed;
	}
	
	@Override
	public boolean remove(Object key, Object value)
	{
		Object k = maskNull(key);
		Object[] tab = this.table;
		int len = tab.length;
		int i = index(k, len);
		
		while (true)
		{
			Object item = tab[i];
			if (item == k)
			{
				if (tab[i + 1] != value)
				{
					return false;
				}
				this.size--;
				tab[i] = null;
				tab[i + 1] = null;
				this.closeDeletion(i);
				return true;
			}
			if (item == null)
			{
				return false;
			}
			i = nextKeyIndex(i, len);
		}
	}
	
	@Override
	public void map(BiFunction<? super K, ? super V, ? extends V> mapper)
	{
		Object[] tab = this.table;
		for (int i = 0; i < tab.length; i += 2)
		{
			Object key = tab[i];
			if (key != null)
			{
				tab[i + 1] = mapper.apply((K) unmaskNull(key), (V) tab[i + 1]);
			}
		}
	}
	
	@Override
	public void filter(BiPredicate<? super K, ? super V> condition)
	{
		Object[] tab = this.table;
		for (int i = 0; i < tab.length; i += 2)
		{
			Object key = tab[i];
			if (key == null)
			{
				continue;
			}
			if (!condition.test((K) unmaskNull(tab[i]), (V) tab[i + 1]))
			{
				tab[i] = tab[i + 1] = null;
				this.closeDeletion(i);
			}
		}
	}
	
	@Override
	public MutableMap<K, V> copy()
	{
		return new IdentityHashMap(this);
	}
	
	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy()
	{
		return new IdentityHashMap(this.size);
	}
	
	@Override
	public ImmutableMap<K, V> immutable()
	{
		return new ArrayMap(this); // TODO immutable.IdentityHashMap
	}
}
