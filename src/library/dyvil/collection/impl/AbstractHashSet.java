package dyvil.collection.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

import dyvil.collection.Collection;
import dyvil.collection.Set;
import dyvil.math.MathUtils;

import static dyvil.collection.impl.AbstractHashMap.MAX_ARRAY_SIZE;
import static dyvil.collection.impl.AbstractHashMap.hash;
import static dyvil.collection.impl.AbstractHashMap.index;

public abstract class AbstractHashSet<E> implements Set<E>
{
	protected static final class HashElement<E>
	{
		public E				element;
		public int				hash;
		public HashElement<E>	next;
		
		public HashElement(E element, int hash)
		{
			this.element = element;
			this.hash = hash;
		}
		
		public HashElement(E element, int hash, HashElement next)
		{
			this.element = element;
			this.hash = hash;
			this.next = next;
		}
	}
	
	protected int			size;
	protected HashElement[]	elements;
	
	public AbstractHashSet()
	{
	}
	
	public AbstractHashSet(Collection<E> collection)
	{
		int length = MathUtils.powerOfTwo(AbstractHashMap.grow(collection.size()));
		HashElement[] elements = this.elements = new HashElement[length];
		int size = 0;
		
		outer:
		for (E element : collection)
		{
			int hash = hash(element);
			int index = index(hash, length);
			for (HashElement e = elements[index]; e != null; e = e.next)
			{
				Object k;
				if (e.hash == hash && ((k = e.element) == element || element != null && element.equals(k)))
				{
					e.element = element;
					continue outer;
				}
			}
			
			elements[index] = new HashElement(element, hash, elements[index]);
			size++;
		}
		
		this.size = size;
	}
	
	public AbstractHashSet(Set<E> set)
	{
		int size = this.size = set.size();
		int length = MathUtils.powerOfTwo(AbstractHashMap.grow(size));
		HashElement[] elements = this.elements = new HashElement[length];
		
		// Assume unique elements
		for (E element : set)
		{
			int hash = hash(element);
			int index = index(hash, length);
			elements[index] = new HashElement(element, hash, elements[index]);
		}
	}
	
	public AbstractHashSet(AbstractHashSet<E> set)
	{
		int size = this.size = set.size();
		int length = MathUtils.powerOfTwo(AbstractHashMap.grow(size));
		HashElement[] elements = this.elements = new HashElement[length];
		
		for (HashElement hashElement : set.elements)
		{
			for (; hashElement != null; hashElement = hashElement.next)
			{
				int hash = hashElement.hash;
				int index = index(hash, length);
				elements[index] = new HashElement(hashElement.element, hash, elements[index]);
			}
		}
	}
	
	public AbstractHashSet(E... elements)
	{
		int length = MathUtils.powerOfTwo(AbstractHashMap.grow(elements.length));
		HashElement[] hashElements = this.elements = new HashElement[length];
		int size = 0;
		
		outer:
		for (E element : elements)
		{
			int hash = hash(element);
			int index = index(hash, length);
			for (HashElement e = hashElements[index]; e != null; e = e.next)
			{
				Object k;
				if (e.hash == hash && ((k = e.element) == element || element != null && element.equals(k)))
				{
					e.element = element;
					continue outer;
				}
			}
			
			hashElements[index] = new HashElement(element, hash, hashElements[index]);
			size++;
		}
		
		this.size = size;
	}
	
	protected void flatten()
	{
		this.ensureCapacityInternal((this.elements.length << 1));
	}
	
	protected void ensureCapacity(int newCapacity)
	{
		this.ensureCapacity(MathUtils.powerOfTwo(newCapacity));
	}
	
	protected void ensureCapacityInternal(int newCapacity)
	{
		HashElement[] oldMap = this.elements;
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
		
		HashElement[] newMap = this.elements = new HashElement[newCapacity];
		
		for (int i = oldCapacity; i-- > 0;)
		{
			HashElement e = oldMap[i];
			while (e != null)
			{
				int index = index(e.hash, newCapacity);
				e.next = newMap[index];
				newMap[index] = e;
				e = e.next;
			}
		}
		
		this.updateThreshold(newCapacity);
	}
	
	protected void updateThreshold(int newCapacity)
	{
	
	}
	
	protected boolean addInternal(E element)
	{
		int hash = hash(element);
		int i = index(hash, this.elements.length);
		for (HashElement<E> e = this.elements[i]; e != null; e = e.next)
		{
			Object k;
			if (e.hash == hash && ((k = e.element) == element || Objects.equals(element, k)))
			{
				return false;
			}
		}
		
		this.addElement(hash, element, i);
		return true;
	}
	
	protected void addElement(int hash, E element, int index)
	{
		this.elements[index] = new HashElement(element, hash, this.elements[index]);
		this.size++;
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
			HashElement<E> next; // next entry to return
			HashElement<E> current; // current entry
			int index; // current slot
			
			{
				HashElement<E>[] t = AbstractHashSet.this.elements;
				this.current = this.next = null;
				this.index = 0;
				// advance to first entry
				if (t != null && AbstractHashSet.this.size > 0)
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
			
			@Override
			public final E next()
			{
				HashElement<E>[] t;
				HashElement<E> e = this.next;
				if (e == null)
				{
					throw new NoSuchElementException();
				}
				if ((this.next = (this.current = e).next) == null && (t = AbstractHashSet.this.elements) != null)
				{
					do
					{
					}
					while (this.index < t.length && (this.next = t[this.index++]) == null);
				}
				return e.element;
			}
			
			@Override
			public final void remove()
			{
				HashElement<E> e = this.current;
				if (e == null)
				{
					throw new IllegalStateException();
				}
				
				AbstractHashSet.this.size--;
				this.current = null;
				int index = index(e.hash, AbstractHashSet.this.elements.length);
				HashElement<E> entry = AbstractHashSet.this.elements[index];
				if (entry == e)
				{
					AbstractHashSet.this.elements[index] = e.next;
				}
				else
				{
					HashElement<E> prev;
					do
					{
						prev = entry;
						entry = entry.next;
					}
					while (entry != e);
					
					prev.next = e.next;
				}
			}
		};
	}
	
	@Override
	public void forEach(Consumer<? super E> action)
	{
		for (HashElement<E> hashElement : this.elements)
		{
			for (; hashElement != null; hashElement = hashElement.next)
			{
				action.accept(hashElement.element);
			}
		}
	}
	
	@Override
	public boolean contains(Object element)
	{
		if (element == null)
		{
			for (HashElement<E> hashElement = this.elements[0]; hashElement != null; hashElement = hashElement.next)
			{
				if (hashElement.element == null)
				{
					return true;
				}
			}
			
			return false;
		}
		
		int hash = hash(element);
		int index = index(hash, this.elements.length);
		for (HashElement<E> hashElement = this.elements[index]; hashElement != null; hashElement = hashElement.next)
		{
			if (hashElement.hash == hash && element.equals(hashElement.element))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public java.util.Set<E> toJava()
	{
		java.util.HashSet<E> set = new java.util.HashSet<E>(this.size);
		for (E element : this)
		{
			set.add(element);
		}
		return set;
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
