package dyvil.collection.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import dyvil.collection.Collection;
import dyvil.collection.Set;
import dyvil.math.MathUtils;
import dyvil.util.ImmutableException;

import static dyvil.collection.impl.AbstractIdentityHashMap.NULL;
import static dyvil.collection.impl.AbstractIdentityHashMap.index;
import static dyvil.collection.impl.AbstractIdentityHashMap.maskNull;
import static dyvil.collection.impl.AbstractIdentityHashMap.unmaskNull;

public abstract class AbstractIdentityHashSet<E> implements Set<E>
{
	private static final long serialVersionUID = -6688373107015354853L;
	
	protected static final int		DEFAULT_CAPACITY	= 12;
	protected static final float	DEFAULT_LOAD_FACTOR	= 2F / 3F;
	
	protected Object[]	table;
	protected int		size;
	
	public AbstractIdentityHashSet()
	{
	}
	
	public AbstractIdentityHashSet(int capacity)
	{
		if (capacity < 0)
		{
			throw new IllegalArgumentException("Invalid Capacity: " + capacity);
		}
		this.table = new Object[MathUtils.powerOfTwo(AbstractHashMap.grow(capacity))];
	}
	
	public AbstractIdentityHashSet(Collection<E> collection)
	{
		this(collection.size());
		for (E element : collection)
		{
			this.addInternal(element);
		}
	}
	
	public AbstractIdentityHashSet(AbstractIdentityHashSet<E> set)
	{
		this.table = set.table.clone();
		this.size = set.size;
	}
	
	public AbstractIdentityHashSet(E... elements)
	{
		this(elements.length);
		for (E element : elements)
		{
			this.addInternal(element);
		}
	}
	
	protected static int nextIndex(int i, int len)
	{
		return i + 1 < len ? i + 1 : 0;
	}
	
	protected void flatten()
	{
		this.ensureCapacityInternal(this.table.length << 1);
	}
	
	public void ensureCapacity(int newCapacity)
	{
		if (newCapacity > this.table.length)
		{
			this.ensureCapacityInternal(MathUtils.powerOfTwo(newCapacity));
		}
	}
	
	protected void ensureCapacityInternal(int newCapacity)
	{
		Object[] oldTable = this.table;
		int oldLength = oldTable.length;
		if (newCapacity - AbstractHashMap.MAX_ARRAY_SIZE > 0)
		{
			if (oldLength == AbstractHashMap.MAX_ARRAY_SIZE)
			{
				return;
			}
			newCapacity = AbstractHashMap.MAX_ARRAY_SIZE;
		}
		
		Object[] newTable = new Object[newCapacity];
		
		for (int j = 0; j < oldLength; j++)
		{
			Object key = oldTable[j];
			if (key != null)
			{
				oldTable[j] = null;
				int i = index(key, newCapacity);
				while (newTable[i] != null)
				{
					i = nextIndex(i, newCapacity);
				}
				newTable[i] = key;
			}
		}
		this.table = newTable;
		
		this.updateThreshold(newCapacity);
	}
	
	protected void updateThreshold(int newCapacity)
	{
	}
	
	protected boolean addInternal(E element)
	{
		Object k = maskNull(element);
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
			i = nextIndex(i, len);
		}
		
		this.addElement(i, k);
		return true;
	}
	
	protected void addElement(int index, Object element)
	{
		this.table[index] = element;
		if (++this.size >= this.table.length * AbstractIdentityHashMap.DEFAULT_LOAD_FACTOR)
		{
			this.flatten();
		}
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
			int index = AbstractIdentityHashSet.this.size != 0 ? 0 : AbstractIdentityHashSet.this.table.length;
			int lastReturnedIndex = -1;
			boolean indexValid;
			Object[] traversalTable = AbstractIdentityHashSet.this.table;
			
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
				return (E) unmaskNull(this.traversalTable[this.lastReturnedIndex]);
			}
			
			@Override
			public void remove()
			{
				if (this.lastReturnedIndex == -1)
				{
					throw new IllegalStateException();
				}
				if (AbstractIdentityHashSet.this.isImmutable())
				{
					throw new ImmutableException("Iterator.remove() on Immutable Set");
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
				
				if (tab != AbstractIdentityHashSet.this.table)
				{
					AbstractIdentityHashSet.this.remove(key);
					return;
				}
				
				AbstractIdentityHashSet.this.size--;
				
				Object item;
				for (int i = nextIndex(d, len); (item = tab[i]) != null; i = nextIndex(i, len))
				{
					int r = index(item, len);
					// See closeDeletion for explanation of this conditional
					if (i < r && (r <= d || d <= i) || r <= d && d <= i)
					{
						if (i < deletedSlot && d >= deletedSlot && this.traversalTable == AbstractIdentityHashSet.this.table)
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
	public void forEach(Consumer<? super E> action)
	{
		for (Object element : this.table)
		{
			if (element != null)
			{
				action.accept((E) unmaskNull(element));
			}
		}
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
	public java.util.Set<E> toJava()
	{
		return new java.util.AbstractSet<E>()
		{
			@Override
			public int size()
			{
				return AbstractIdentityHashSet.this.size;
			}
			
			@Override
			public Iterator<E> iterator()
			{
				return AbstractIdentityHashSet.this.iterator();
			}
			
			@Override
			public boolean contains(Object o)
			{
				return AbstractIdentityHashSet.this.contains(o);
			}
			
			@Override
			public void clear()
			{
				AbstractIdentityHashSet.this.clear();
			}
			
			@Override
			public boolean add(E e)
			{
				return AbstractIdentityHashSet.this.add(e);
			}
			
			@Override
			public boolean remove(Object o)
			{
				return AbstractIdentityHashSet.this.remove(o);
			}
		};
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
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
		
		int len = this.table.length;
		
		out.writeInt(this.size);
		out.writeInt(len);
		
		for (int i = 0; i < len; i++)
		{
			// Avoid the NULL object
			Object key = this.table[i];
			if (key != null)
			{
				out.writeObject(unmaskNull(key));
			}
		}
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		
		this.size = in.readInt();
		this.table = new Object[in.readInt()];
		
		// Read (size) key-value pairs and put them in this map
		for (int i = 0; i < this.size; i++)
		{
			this.addInternal((E) in.readObject());
		}
	}
}
