package dyvil.maps.primitive;

import java.util.Objects;

public class DoubleHashMap<V>
{
	protected Entry[]		hashArray;
	protected int			size;
	protected int			capacity;
	protected final float	loadFactor;
	protected volatile int	modCount;
	
	public DoubleHashMap()
	{
		this(16, 0.75F);
	}
	
	public DoubleHashMap(int size, float loadFactor)
	{
		this.capacity = (int) (size * loadFactor);
		this.loadFactor = loadFactor;
		this.hashArray = new Entry[size];
	}
	
	private static int getHashedKey(double key)
	{
		long bits = Double.doubleToLongBits(key);
		return hash((int) (bits ^ bits >>> 32));
	}
	
	private static int hash(int hash)
	{
		hash ^= hash >>> 20 ^ hash >>> 12;
		return hash ^ hash >>> 7 ^ hash >>> 4;
	}
	
	private static int getHashIndex(int hash, int size)
	{
		return hash & size - 1;
	}
	
	public int size()
	{
		return this.size;
	}
	
	public V get(double key)
	{
		int i = getHashedKey(key);
		for (Entry<V> entry = this.hashArray[getHashIndex(i, this.hashArray.length)]; entry != null; entry = entry.next)
		{
			if (entry.key == key)
			{
				return entry.value;
			}
		}
		return null;
	}
	
	public boolean contains(double key)
	{
		return this.getEntry(key) != null;
	}
	
	protected final Entry<V> getEntry(double key)
	{
		int i = getHashedKey(key);
		for (Entry localEntry = this.hashArray[getHashIndex(i, this.hashArray.length)]; localEntry != null; localEntry = localEntry.next)
		{
			if (localEntry.key == key)
			{
				return localEntry;
			}
		}
		return null;
	}
	
	public V put(double key, V paramObject)
	{
		int i = getHashedKey(key);
		int j = getHashIndex(i, this.hashArray.length);
		for (Entry<V> localEntry = this.hashArray[j]; localEntry != null; localEntry = localEntry.next)
		{
			if (localEntry.key == key)
			{
				V v = localEntry.value;
				localEntry.value = paramObject;
				return v;
			}
		}
		
		this.size++;
		this.modCount++;
		this.createKey(i, key, paramObject, j);
		
		return null;
	}
	
	private void resizeTable(int newSize)
	{
		Entry[] src = this.hashArray;
		int i = src.length;
		
		if (i == 1073741824)
		{
			this.capacity = 2147483647;
			return;
		}
		
		Entry[] dest = new Entry[newSize];
		this.copyHashTableTo(dest);
		this.hashArray = dest;
		this.capacity = (int) (newSize * this.loadFactor);
	}
	
	private void copyHashTableTo(Entry[] dest)
	{
		Entry[] src = this.hashArray;
		int i = dest.length;
		for (int j = 0; j < src.length; ++j)
		{
			Entry entry = src[j];
			if (entry != null)
			{
				src[j] = null;
				do
				{
					Entry localEntry = entry.next;
					int k = getHashIndex(entry.hash, i);
					entry.next = dest[k];
					dest[k] = entry;
					entry = localEntry;
				}
				while (entry != null);
			}
		}
	}
	
	public V remove(double key)
	{
		Entry<V> localEntry = this.removeKey(key);
		return localEntry == null ? null : localEntry.value;
	}
	
	protected final Entry<V> removeKey(double key)
	{
		int hash = getHashedKey(key);
		int index = getHashIndex(hash, this.hashArray.length);
		Entry entry1 = this.hashArray[index];
		Entry entry2 = entry1;
		
		while (entry2 != null)
		{
			Entry next = entry2.next;
			if (entry2.key == key)
			{
				this.modCount++;
				this.size--;
				if (entry1 == entry2)
				{
					this.hashArray[index] = next;
				}
				else
				{
					entry1.next = next;
				}
				return entry2;
			}
			entry1 = entry2;
			entry2 = next;
		}
		
		return entry2;
	}
	
	private void createKey(int hash, double key, V object, int index)
	{
		Entry localEntry = this.hashArray[index];
		this.hashArray[index] = new Entry(hash, key, object, localEntry);
		if (this.size++ >= this.capacity)
		{
			this.resizeTable(2 * this.hashArray.length);
		}
	}
	
	static class Entry<V>
	{
		final double	key;
		final int		hash;
		V				value;
		Entry			next;
		
		protected Entry(int hash, double key, V value, Entry next)
		{
			this.value = value;
			this.next = next;
			this.key = key;
			this.hash = hash;
		}
		
		public final double getKey()
		{
			return this.key;
		}
		
		public final Object getValue()
		{
			return this.value;
		}
		
		@Override
		public final boolean equals(Object other)
		{
			if (!(other instanceof Entry))
			{
				return false;
			}
			Entry entry = (Entry) other;
			if (this.getKey() == entry.getKey())
			{
				if (Objects.equals(this.getValue(), entry.getValue()))
				{
					return true;
				}
			}
			return false;
		}
		
		@Override
		public final int hashCode()
		{
			return this.hash;
		}
		
		@Override
		public final String toString()
		{
			return this.getKey() + "=" + this.getValue();
		}
	}
}
