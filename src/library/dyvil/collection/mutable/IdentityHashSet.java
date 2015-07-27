package dyvil.collection.mutable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.collection.Collection;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.Set;
import dyvil.collection.immutable.ArraySet;
import dyvil.collection.impl.AbstractHashMap;
import dyvil.math.MathUtils;

import static dyvil.collection.mutable.IdentityHashMap.*;

public class IdentityHashSet<E> implements MutableSet<E>
{
	private Object[]	table;
	private int			size;
	private int			threshold;
	
	public static <E> IdentityHashSet<E> apply()
	{
		return new IdentityHashSet();
	}
	
	IdentityHashSet(int size, int treshold, Object[] table)
	{
		this.size = size;
		this.threshold = treshold;
		this.table = table;
	}
	
	public IdentityHashSet()
	{
		this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
	}
	
	public IdentityHashSet(int size)
	{
		this(size, DEFAULT_LOAD_FACTOR);
	}
	
	public IdentityHashSet(float loadFactor)
	{
		this(DEFAULT_CAPACITY, loadFactor);
	}
	
	public IdentityHashSet(int size, float loadFactor)
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
		this.threshold = (int) Math.min(size * loadFactor, AbstractHashMap.MAX_ARRAY_SIZE + 1);
	}
	
	public IdentityHashSet(Collection<E> collection)
	{
		this(AbstractHashMap.grow(collection.size()), DEFAULT_LOAD_FACTOR);
		for (E element : collection)
		{
			this.$plus$eq(element);
		}
	}
	
	private void resize(int size)
	{
		int newLength = size << 1;
		
		Object[] oldTable = this.table;
		int oldLength = oldTable.length;
		if (newLength - AbstractHashMap.MAX_ARRAY_SIZE > 0)
		{
			if (oldLength == AbstractHashMap.MAX_ARRAY_SIZE)
			{
				return;
			}
			newLength = AbstractHashMap.MAX_ARRAY_SIZE;
		}
		
		Object[] newTable = new Object[newLength];
		this.threshold = newLength / 3;
		
		for (int j = 0; j < oldLength; j++)
		{
			Object key = oldTable[j];
			if (key != null)
			{
				oldTable[j] = null;
				int i = hash(key, newLength);
				while (newTable[i] != null)
				{
					i = nextIndex(i, newLength);
				}
				newTable[i] = key;
			}
		}
		this.table = newTable;
	}
	
	private static int nextIndex(int i, int len)
	{
		return i + 1 < len ? i + 1 : 0;
	}
	
	@Override
	public int size()
	{
		return this.size;
	}
	
	@Override
	public Iterator<E> iterator()
	{
		return new Iterator<E>()
		{
			int index = IdentityHashSet.this.size != 0 ? 0 : IdentityHashSet.this.table.length;
			int lastReturnedIndex = -1;
			boolean indexValid;
			Object[] traversalTable = IdentityHashSet.this.table;
			
			@Override
			public boolean hasNext()
			{
				Object[] tab = this.traversalTable;
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
			
			@Override
			public E next()
			{
				if (!this.indexValid && !this.hasNext())
				{
					throw new NoSuchElementException();
				}
				
				this.indexValid = false;
				this.lastReturnedIndex = this.index;
				this.index++;
				return (E) this.traversalTable[this.lastReturnedIndex];
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
				tab[d] = null;
				
				if (tab != IdentityHashSet.this.table)
				{
					IdentityHashSet.this.remove(key);
					return;
				}
				
				IdentityHashSet.this.size--;
				
				Object item;
				for (int i = nextIndex(d, len); (item = tab[i]) != null; i = nextIndex(i, len))
				{
					int r = hash(item, len);
					// See closeDeletion for explanation of this conditional
					if (i < r && (r <= d || d <= i) || r <= d && d <= i)
					{
						if (i < deletedSlot && d >= deletedSlot && this.traversalTable == IdentityHashSet.this.table)
						{
							int remaining = len - deletedSlot;
							Object[] newTable = new Object[remaining];
							System.arraycopy(tab, deletedSlot, newTable, 0, remaining);
							this.traversalTable = newTable;
							this.index = 0;
						}
						
						tab[d] = item;
						tab[i] = null;
						d = i;
					}
				}
			}
		};
	}
	
	@Override
	public boolean contains(Object element)
	{
		if (element == null)
		{
			for (Object o : this.table)
			{
				if (o == NULL)
				{
					return true;
				}
			}
			return false;
		}
		
		for (Object o : this.table)
		{
			if (element == o)
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void clear()
	{
		this.size = 0;
		for (int i = 0; i < this.table.length; i++)
		{
			this.table[i] = null;
		}
	}
	
	@Override
	public boolean add(E element)
	{
		Object k = maskNull(element);
		Object[] tab = this.table;
		int len = tab.length;
		int i = hash(k, len);
		
		Object item;
		while ((item = tab[i]) != null)
		{
			if (item == k)
			{
				return false;
			}
			i = nextIndex(i, len);
		}
		
		tab[i] = k;
		if (++this.size >= this.threshold)
		{
			this.resize(len);
		}
		return true;
	}
	
	private void closeDeletion(int index)
	{
		Object[] tab = this.table;
		int len = tab.length;
		
		Object item;
		for (int i = nextIndex(index, len); (item = tab[i]) != null; i = nextIndex(i, len))
		{
			int r = hash(item, len);
			if (i < r && (r <= index || index <= i) || r <= index && index <= i)
			{
				tab[index] = item;
				tab[i] = null;
				index = i;
			}
		}
	}
	
	@Override
	public boolean remove(Object key)
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
				tab[i] = null;
				this.closeDeletion(i);
				return true;
			}
			if (item == null)
			{
				return false;
			}
			i = nextIndex(i, len);
		}
	}
	
	@Override
	public void map(Function<? super E, ? extends E> mapper)
	{
		for (int i = 0; i < this.table.length; i++)
		{
			Object o = this.table[i];
			if (o != null)
			{
				this.table[i] = mapper.apply((E) unmaskNull(o));
			}
		}
	}
	
	@Override
	public void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper)
	{
	}
	
	@Override
	public void filter(Predicate<? super E> condition)
	{
		for (int i = 0; i < this.table.length; i++)
		{
			Object o = this.table[i];
			if (o != null && !condition.test((E) unmaskNull(o)))
			{
				this.table[i] = null;
			}
		}
	}
	
	@Override
	public void toArray(int index, Object[] store)
	{
		for (Object o : this.table)
		{
			if (o != null)
			{
				store[index++] = unmaskNull(o);
			}
		}
	}
	
	@Override
	public MutableSet<E> copy()
	{
		int newLen = MathUtils.powerOfTwo(AbstractHashMap.grow(this.size));
		Object[] newTable = new Object[newLen];
		for (Object o : this.table)
		{
			if (o != null)
			{
				int index = hash(o, newLen);
				while (newTable[index] != null)
				{
					index = nextIndex(index, newLen);
				}
				newTable[index] = o;
			}
		}
		
		return new IdentityHashSet(this.size, this.threshold, newTable);
	}
	
	@Override
	public <R> MutableSet<R> emptyCopy()
	{
		return new IdentityHashSet(this.size);
	}
	
	@Override
	public ImmutableSet<E> immutable()
	{
		return new ArraySet<E>(this); // TODO immutable.IdentityHashSet
	}
	
	@Override
	public String toString()
	{
		return Collection.collectionToString(this);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return Set.setEquals(this, obj);
	}
	
	@Override
	public int hashCode()
	{
		return Set.setHashCode(this);
	}
}
