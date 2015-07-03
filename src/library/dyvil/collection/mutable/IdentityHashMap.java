package dyvil.collection.mutable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import dyvil.lang.Entry;
import dyvil.lang.Map;
import dyvil.lang.literal.NilConvertible;

import dyvil.collection.ImmutableMap;
import dyvil.collection.MutableMap;
import dyvil.math.MathUtils;

@NilConvertible
public class IdentityHashMap<K, V> implements MutableMap<K, V>
{
	private final class TableEntry implements Entry<K, V>
	{
		int	index;
		
		public TableEntry(int index)
		{
			this.index = index;
		}
		
		@Override
		public K getKey()
		{
			return (K) IdentityHashMap.this.table[this.index];
		}
		
		@Override
		public V getValue()
		{
			return (V) IdentityHashMap.this.table[this.index + 1];
		}
		
		@Override
		public String toString()
		{
			return IdentityHashMap.this.table[this.index] + " -> " + IdentityHashMap.this.table[this.index + 1];
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
	
	private abstract class TableIterator<E> implements Iterator<E>
	{
		int			index				= IdentityHashMap.this.size != 0 ? 0 : IdentityHashMap.this.table.length;
		int			lastReturnedIndex	= -1;
		boolean		indexValid;
		Object[]	traversalTable		= IdentityHashMap.this.table;
		
		@Override
		public boolean hasNext()
		{
			Object[] tab = this.traversalTable;
			for (int i = this.index; i < tab.length; i += 2)
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
			this.lastReturnedIndex = this.index;
			this.index += 2;
			return this.lastReturnedIndex;
		}
		
		@Override
		public void remove()
		{
			if (this.lastReturnedIndex == -1)
			{
				throw new IllegalStateException();
			}
			
			int deletedSlot = this.lastReturnedIndex;
			this.lastReturnedIndex = -1;
			// back up index to revisit new contents after deletion
			this.index = deletedSlot;
			this.indexValid = false;
			
			Object[] tab = this.traversalTable;
			int len = tab.length;
			
			int d = deletedSlot;
			Object key = tab[d];
			tab[d] = null; // vacate the slot
			tab[d + 1] = null;
			
			if (tab != IdentityHashMap.this.table)
			{
				IdentityHashMap.this.removeKey(key);
				return;
			}
			
			IdentityHashMap.this.size--;
			
			Object item;
			for (int i = nextKeyIndex(d, len); (item = tab[i]) != null; i = nextKeyIndex(i, len))
			{
				int r = hash(item, len);
				// See closeDeletion for explanation of this conditional
				if (i < r && (r <= d || d <= i) || r <= d && d <= i)
				{
					if (i < deletedSlot && d >= deletedSlot && this.traversalTable == IdentityHashMap.this.table)
					{
						int remaining = len - deletedSlot;
						Object[] newTable = new Object[remaining];
						System.arraycopy(tab, deletedSlot, newTable, 0, remaining);
						this.traversalTable = newTable;
						this.index = 0;
					}
					
					tab[d] = item;
					tab[d + 1] = tab[i + 1];
					tab[i] = null;
					tab[i + 1] = null;
					d = i;
				}
			}
		}
	}
	
	static final int	DEFAULT_CAPACITY	= 12;
	static final float	DEFAULT_LOAD_FACTOR	= 2F / 3F;
	
	private Object[]	table;
	private int			size;
	private int			threshold;
	
	public static <K, V> IdentityHashMap<K, V> apply()
	{
		return new IdentityHashMap();
	}
	
	IdentityHashMap(int size, int treshold, Object[] table)
	{
		this.size = size;
		this.threshold = treshold;
		this.table = table;
	}
	
	public IdentityHashMap()
	{
		this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
	}
	
	public IdentityHashMap(int size)
	{
		this(size, DEFAULT_LOAD_FACTOR);
	}
	
	public IdentityHashMap(float loadFactor)
	{
		this(DEFAULT_CAPACITY, loadFactor);
	}
	
	public IdentityHashMap(int size, float loadFactor)
	{
		if (size < 0)
		{
			throw new IllegalArgumentException("Invalid Capacity: " + size);
		}
		if (loadFactor <= 0 || Float.isNaN(loadFactor))
		{
			throw new IllegalArgumentException("Invalid Load Factor: " + loadFactor);
		}
		
		this.table = new Object[MathUtils.powerOfTwo(size << 1)];
		this.threshold = (int) Math.min(size * loadFactor, HashMap.MAX_ARRAY_SIZE + 1);
	}
	
	public IdentityHashMap(Map<K, V> map)
	{
		this(map.size(), DEFAULT_LOAD_FACTOR);
		for (Entry<K, V> entry : map)
		{
			this.subscript_$eq(entry.getKey(), entry.getValue());
		}
	}
	
	@Override
	public int size()
	{
		return this.size;
	}
	
	@Override
	public Iterator<Entry<K, V>> iterator()
	{
		return new TableIterator<Entry<K, V>>()
		{
			@Override
			public Entry<K, V> next()
			{
				return new TableEntry(this.nextIndex());
			}
			
			@Override
			public String toString()
			{
				return "EntryIterator(" + IdentityHashMap.this + ")";
			}
		};
	}
	
	@Override
	public Iterator<K> keyIterator()
	{
		return new TableIterator<K>()
		{
			@Override
			public K next()
			{
				return (K) IdentityHashMap.this.table[this.nextIndex()];
			}
			
			@Override
			public String toString()
			{
				return "KeyIterator(" + IdentityHashMap.this + ")";
			}
		};
	}
	
	@Override
	public Iterator<V> valueIterator()
	{
		return new TableIterator<V>()
		{
			@Override
			public V next()
			{
				return (V) IdentityHashMap.this.table[this.nextIndex() + 1];
			}
			
			@Override
			public String toString()
			{
				return "ValueIterator(" + IdentityHashMap.this + ")";
			}
		};
	}
	
	static final Object	NULL	= new Object();
	
	static Object maskNull(Object o)
	{
		return o == null ? NULL : o;
	}
	
	static Object unmaskNull(Object o)
	{
		return o == NULL ? null : o;
	}
	
	static int hash(Object x, int length)
	{
		int h = System.identityHashCode(x);
		h = (h << 1) - (h << 8); // Multiply by -127
		return h & length - 1;
	}
	
	private static int nextKeyIndex(int i, int len)
	{
		return i + 2 < len ? i + 2 : 0;
	}
	
	private void resize(int size)
	{
		int newLength = size << 1;
		
		Object[] oldTable = this.table;
		int oldLength = oldTable.length;
		if (newLength - HashMap.MAX_ARRAY_SIZE > 0)
		{
			if (oldLength == HashMap.MAX_ARRAY_SIZE)
			{
				return;
			}
			newLength = HashMap.MAX_ARRAY_SIZE;
		}
		
		Object[] newTable = new Object[newLength];
		this.threshold = newLength / 3;
		
		for (int j = 0; j < oldLength; j += 2)
		{
			Object key = oldTable[j];
			if (key != null)
			{
				Object value = oldTable[j + 1];
				oldTable[j] = null;
				oldTable[j + 1] = null;
				int i = hash(key, newLength);
				while (newTable[i] != null)
				{
					i = nextKeyIndex(i, newLength);
				}
				newTable[i] = key;
				newTable[i + 1] = value;
			}
		}
		this.table = newTable;
	}
	
	@Override
	public boolean containsKey(Object key)
	{
		Object k = maskNull(key);
		Object[] tab = this.table;
		int len = tab.length;
		int i = hash(k, len);
		while (true)
		{
			Object item = tab[i];
			if (item == k)
			{
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
	public boolean contains(Object key, Object value)
	{
		Object k = maskNull(key);
		Object[] tab = this.table;
		int len = tab.length;
		int i = hash(k, len);
		while (true)
		{
			Object item = tab[i];
			if (item == k)
			{
				return tab[i + 1] == value;
			}
			if (item == null)
			{
				return false;
			}
			i = nextKeyIndex(i, len);
		}
	}
	
	@Override
	public boolean containsValue(Object value)
	{
		Object[] tab = this.table;
		for (int i = 1; i < tab.length; i += 2)
		{
			if (tab[i] == value && tab[i - 1] != null)
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public V get(Object key)
	{
		Object k = maskNull(key);
		Object[] tab = this.table;
		int len = tab.length;
		int i = hash(k, len);
		while (true)
		{
			Object item = tab[i];
			if (item == k)
			{
				return (V) tab[i + 1];
			}
			if (item == null)
			{
				return null;
			}
			i = nextKeyIndex(i, len);
		}
	}
	
	@Override
	public MutableMap<K, V> $plus(K key, V value)
	{
		MutableMap<K, V> copy = this.copy();
		copy.subscript_$eq(key, value);
		return copy;
	}
	
	@Override
	public MutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map)
	{
		MutableMap<K, V> copy = this.copy();
		copy.$plus$plus$eq(map);
		return copy;
	}
	
	@Override
	public MutableMap<K, V> $minus(Object key)
	{
		MutableMap<K, V> copy = this.copy();
		copy.$minus$eq(key);
		return copy;
	}
	
	@Override
	public MutableMap<K, V> $minus(Object key, Object value)
	{
		MutableMap<K, V> copy = this.copy();
		copy.remove(key, value);
		return copy;
	}
	
	@Override
	public MutableMap<K, V> $minus$colon(Object value)
	{
		MutableMap<K, V> copy = this.copy();
		copy.$minus$colon$eq(value);
		return copy;
	}
	
	@Override
	public MutableMap<K, V> $minus$minus(Map<? super K, ? super V> map)
	{
		MutableMap<K, V> copy = this.copy();
		copy.$minus$minus$eq(map);
		return copy;
	}
	
	@Override
	public <U> MutableMap<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper)
	{
		MutableMap<K, U> copy = (MutableMap<K, U>) this.copy();
		copy.map((BiFunction<? super K, ? super U, ? extends U>) mapper);
		return copy;
	}
	
	@Override
	public MutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		MutableMap<K, V> copy = this.copy();
		copy.filter(condition);
		return copy;
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
		Object k = maskNull(key);
		Object[] tab = this.table;
		int len = tab.length;
		int i = hash(k, len);
		
		Object item;
		while ((item = tab[i]) != null)
		{
			if (item == k)
			{
				@SuppressWarnings("unchecked")
				V oldValue = (V) tab[i + 1];
				tab[i + 1] = value;
				return oldValue;
			}
			i = nextKeyIndex(i, len);
		}
		
		tab[i] = k;
		tab[i + 1] = value;
		if (++this.size >= this.threshold)
		{
			this.resize(len);
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
			int r = hash(item, len);
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
		int i = hash(k, len);
		
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
		int i = hash(k, len);
		
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
			tab[i + 1] = mapper.apply((K) tab[i], (V) tab[i + 1]);
		}
	}
	
	@Override
	public void filter(BiPredicate<? super K, ? super V> condition)
	{
		Object[] tab = this.table;
		for (int i = 0; i < tab.length; i += 2)
		{
			if (!condition.test((K) tab[i], (V) tab[i + 1]))
			{
				tab[i] = tab[i + 1] = null;
				this.closeDeletion(i);
			}
		}
	}
	
	@Override
	public MutableMap<K, V> copy()
	{
		return new IdentityHashMap(this.size, this.threshold, this.table.clone());
	}
	
	@Override
	public ImmutableMap<K, V> immutable()
	{
		return null; // TODO immutable.IdentityHashMap
	}
	
	@Override
	public String toString()
	{
		if (this.size == 0)
		{
			return "[]";
		}
		
		StringBuilder builder = new StringBuilder("[ ");
		int i = 0;
		Object[] tab = this.table;
		for (; i < tab.length; i += 2)
		{
			Object key = tab[i];
			if (key != null)
			{
				builder.append(key).append(" -> ").append(tab[i + 1]);
				break;
			}
		}
		
		for (i += 2; i < tab.length; i += 2)
		{
			Object key = tab[i];
			if (key != null)
			{
				builder.append(", ").append(key).append(" -> ").append(tab[i + 1]);
			}
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
