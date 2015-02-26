package dyvil.collections.mutable;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;

import dyvil.arrays.ArrayUtils;

public class ArraySet<E> extends AbstractSet<E>
{
	private static final int	MAX_ARRAY_SIZE	= Integer.MAX_VALUE - 8;
	
	private Object[]			entries;
	
	private int					initialCapacity;
	private int					size;
	
	private int					currentIndex;
	
	private transient Object[]	toArray;
	
	public ArraySet()
	{
		this(16);
	}
	
	public ArraySet(int capacity)
	{
		this.initialCapacity = capacity;
		this.entries = new Object[capacity];
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
	public boolean contains(Object o)
	{
		return ArrayUtils.contains(this.entries, o);
	}
	
	@Override
	public Iterator<E> iterator()
	{
		return new ArraySetIterator();
	}
	
	public class ArraySetIterator implements Iterator
	{
		int	currentIndex	= 0;
		
		@Override
		public boolean hasNext()
		{
			return this.currentIndex < ArraySet.this.size;
		}
		
		@Override
		public E next()
		{
			E e = (E) ArraySet.this.entries[this.currentIndex];
			this.currentIndex++;
			return e;
		}
		
		@Override
		public void remove()
		{
			ArraySet.this.remove(ArraySet.this.entries[this.currentIndex]);
		}
	}
	
	@Override
	public Object[] toArray()
	{
		if (this.toArray != null)
		{
			return this.toArray;
		}
		else
		{
			Object[] o = new Object[this.size];
			for (int i = 0, i0 = 0; i < this.entries.length; i++)
			{
				if (this.entries[i] != null)
				{
					o[i0] = this.entries[i];
					i0++;
				}
			}
			java.util.Arrays.sort(o);
			return this.toArray = o;
		}
	}
	
	@Override
	public <T> T[] toArray(T[] a)
	{
		for (int i = 0; i < this.size; i++)
		{
			a[i] = (T) this.entries[i];
		}
		return a;
	}
	
	@Override
	public boolean add(E e)
	{
		if (e != null && !this.contains(e))
		{
			this.ensureCapacity(++this.size);
			
			this.currentIndex = this.nextIndex();
			this.entries[this.currentIndex] = e;
			this.onChanged();
			return true;
		}
		return false;
	}
	
	protected void onChanged()
	{
		this.toArray = null;
	}
	
	protected void ensureCapacity(int minCapacity)
	{
		if (minCapacity - this.entries.length > 0)
		{
			int oldCapacity = this.entries.length;
			int newCapacity = oldCapacity + (oldCapacity >> 1);
			if (newCapacity - minCapacity < 0)
			{
				newCapacity = this.initialCapacity;
			}
			if (newCapacity - MAX_ARRAY_SIZE > 0)
			{
				newCapacity = hugeCapacity(minCapacity);
			}
			this.entries = ArrayUtils.copy(this.entries, newCapacity);
		}
	}
	
	private static int hugeCapacity(int minCapacity)
	{
		if (minCapacity < 0)
		{
			throw new OutOfMemoryError();
		}
		return minCapacity > MAX_ARRAY_SIZE ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
	}
	
	protected int nextIndex()
	{
		for (int i = 0; i < this.entries.length; i++)
		{
			if (this.entries[i] == null)
			{
				return i;
			}
		}
		return this.currentIndex++;
	}
	
	@Override
	public boolean remove(Object o)
	{
		int index = ArrayUtils.indexOf(this.entries, o);
		if (index != -1)
		{
			this.entries[index] = null;
			this.currentIndex = index;
			--this.size;
			this.onChanged();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean containsAll(Collection<?> c)
	{
		for (Object object : c)
		{
			if (!ArrayUtils.contains(this.entries, object))
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		boolean value = false;
		for (E e : c)
		{
			if (this.add(e))
			{
				value = true;
			}
		}
		return value;
	}
	
	@Override
	public boolean retainAll(Collection<?> c)
	{
		Iterator iterator = c.iterator();
		Object entry = null;
		
		while (iterator.hasNext())
		{
			entry = iterator.next();
			if (!this.contains(entry))
			{
				this.remove(entry);
			}
		}
		
		return false;
	}
	
	@Override
	public boolean removeAll(Collection<?> c)
	{
		boolean value = false;
		for (Object o : c)
		{
			if (this.remove(o))
			{
				value = true;
			}
		}
		return value;
	}
	
	@Override
	public void clear()
	{
		this.size = 0;
		this.entries = new Object[this.initialCapacity];
		this.onChanged();
	}
}
