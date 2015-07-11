package dyvil.collection.mutable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

import dyvil.collection.Collection;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.Set;
import dyvil.collection.immutable.ArraySet;
import dyvil.math.MathUtils;

import static dyvil.collection.mutable.HashMap.*;

public class HashSet<E> implements MutableSet<E>
{
	private static final class HashElement<E>
	{
		E				element;
		int				hash;
		HashElement<E>	next;
		
		HashElement(E element, int hash)
		{
			this.element = element;
			this.hash = hash;
		}
		
		HashElement(E element, int hash, HashElement next)
		{
			this.element = element;
			this.hash = hash;
			this.next = next;
		}
	}
	
	protected int			size;
	private float			loadFactor;
	private int				threshold;
	protected HashElement[]	elements;
	
	HashSet(int size, float loadFactor, HashElement[] elements)
	{
		this.size = size;
		this.loadFactor = loadFactor;
		this.threshold = (int) ((size << 1) / loadFactor);
		this.elements = elements;
	}
	
	public HashSet()
	{
		this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
	}
	
	public HashSet(int size)
	{
		this(size, DEFAULT_LOAD_FACTOR);
	}
	
	public HashSet(float loadFactor)
	{
		this(DEFAULT_CAPACITY, loadFactor);
	}
	
	public HashSet(int size, float loadFactor)
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
		this.elements = new HashElement[MathUtils.powerOfTwo(size)];
		this.threshold = (int) Math.min(size * loadFactor, MAX_ARRAY_SIZE + 1);
	}
	
	public HashSet(Collection<E> collection)
	{
		this(collection.size(), DEFAULT_LOAD_FACTOR);
		for (E element : collection)
		{
			this.add(element);
		}
	}
	
	protected void rehash()
	{
		HashElement[] oldMap = this.elements;
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
		HashElement[] newMap = new HashElement[newCapacity];
		
		this.threshold = (int) Math.min(newCapacity * this.loadFactor, MAX_ARRAY_SIZE + 1);
		this.elements = newMap;
		
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
			HashElement<E>	next;		// next entry to return
			HashElement<E>	current;	// current entry
			int				index;		// current slot
										
			{
				HashElement<E>[] t = HashSet.this.elements;
				this.current = this.next = null;
				this.index = 0;
				// advance to first entry
				if (t != null && HashSet.this.size > 0)
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
				if ((this.next = (this.current = e).next) == null && (t = HashSet.this.elements) != null)
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
				
				HashSet.this.size--;
				this.current = null;
				int index = index(e.hash, HashSet.this.elements.length);
				HashElement<E> entry = HashSet.this.elements[index];
				if (entry == e)
				{
					HashSet.this.elements[index] = e.next;
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
	public void clear()
	{
		this.size = 0;
		int length = this.elements.length;
		for (int i = 0; i < length; i++)
		{
			this.elements[i] = null;
		}
	}
	
	protected void addElement(int hash, E element, int index)
	{
		HashElement[] tab = this.elements;
		if (this.size >= this.threshold)
		{
			// Rehash the table if the threshold is exceeded
			this.rehash();
			
			tab = this.elements;
			hash = hash(element);
			index = index(hash, tab.length);
		}
		
		tab[index] = new HashElement(element, hash, tab[index]);
		this.size++;
	}
	
	@Override
	public boolean add(E element)
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
	
	@Override
	public boolean remove(Object element)
	{
		int hash = hash(element);
		int i = index(hash, this.elements.length);
		HashElement<E> prev = this.elements[i];
		HashElement<E> e = prev;
		
		while (e != null)
		{
			HashElement<E> next = e.next;
			Object k;
			if (e.hash == hash && ((k = e.element) == element || Objects.equals(element, k)))
			{
				this.size--;
				if (prev == e)
				{
					this.elements[i] = next;
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
	public void map(Function<? super E, ? extends E> mapper)
	{
		// Other than flatMap, map allows us to inline the implementation,
		// because we can be sure that the size will not grow, and no re-hash /
		// table growing will be required.
		int len = MathUtils.powerOfTwo(this.size);
		HashElement[] newElements = new HashElement[len];
		int size = 0;
		
		for (HashElement<E> element : this.elements)
		{
			outer:
			for (; element != null; element = element.next)
			{
				E newElement = mapper.apply(element.element);
				int hash = hash(newElement);
				int index = index(hash, len);
				HashElement oldElement = newElements[index];
				
				for (; oldElement != null; oldElement = oldElement.next)
				{
					if (oldElement.hash == hash && Objects.equals(oldElement.element, newElement))
					{
						// Object already present, don't add it
						continue outer;
					}
				}
				
				size++;
				newElements[index] = new HashElement<E>(newElement, hash, newElements[index]);
			}
		}
		
		this.elements = newElements;
		this.size = size;
	}
	
	@Override
	public void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper)
	{
		// To simplify the implementation of this method, we create a temporary
		// copy that is used to collect all new elements produced by the mapper.
		HashSet<E> copy = new HashSet(this.size << 2, this.loadFactor);
		for (E element : this)
		{
			for (E newElement : mapper.apply(element))
			{
				copy.add(newElement);
			}
		}
		
		// After supplying all elements to the wrapper, we simply use the
		// elements of the copy, which will be discarded anyway.
		this.size = copy.size;
		this.elements = copy.elements;
		this.threshold = copy.threshold;
	}
	
	@Override
	public MutableSet<E> copy()
	{
		int len = MathUtils.powerOfTwo(HashMap.grow(this.size));
		HashElement[] newEntries = new HashElement[len];
		for (HashElement<E> e : this.elements)
		{
			while (e != null)
			{
				int index = index(e.hash, len);
				HashElement<E> newEntry = new HashElement<E>(e.element, e.hash);
				if (newEntries[index] != null)
				{
					newEntry.next = newEntries[index];
				}
				
				newEntries[index] = newEntry;
				e = e.next;
			}
		}
		
		return new HashSet<E>(this.size, this.loadFactor, newEntries);
	}
	
	@Override
	public <R> MutableSet<R> emptyCopy()
	{
		return new HashSet(this.size);
	}
	
	@Override
	public ImmutableSet<E> immutable()
	{
		return new ArraySet<E>(this); // TODO immutable.HashSet
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
