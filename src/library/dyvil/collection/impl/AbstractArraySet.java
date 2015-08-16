package dyvil.collection.impl;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

import dyvil.collection.Collection;
import dyvil.collection.Set;

public abstract class AbstractArraySet<E> implements Set<E>
{
	protected Object[]	elements;
	protected int		size;
	
	public AbstractArraySet(Object... elements)
	{
		this.elements = elements.clone();
		this.size = Set.distinct(this.elements, elements.length);
	}
	
	public AbstractArraySet(Object[] elements, int size)
	{
		this.elements = new Object[size];
		System.arraycopy(elements, 0, this.elements, 0, size);
		this.size = Set.distinct(this.elements, size);
	}
	
	public AbstractArraySet(Object[] elements, int size, boolean trusted)
	{
		this.elements = elements;
		this.size = Set.distinct(elements, size);
	}
	
	public AbstractArraySet(Collection<E> elements)
	{
		Object[] array = new Object[elements.size()];
		int index = 0;
		outer:
		for (E element : elements)
		{
			// Check if the element is already present in the array
			for (int i = 0; i < index; i++)
			{
				if (Objects.equals(array[i], element))
				{
					continue outer;
				}
			}
			
			array[index++] = element;
		}
		
		this.elements = array;
		this.size = index;
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
	public boolean isSorted()
	{
		return Collection.isSorted((E[]) this.elements, this.size);
	}
	
	@Override
	public boolean isSorted(Comparator<? super E> comparator)
	{
		return Collection.isSorted((E[]) this.elements, this.size, comparator);
	}
	
	@Override
	public Iterator<E> iterator()
	{
		return new Iterator<E>()
		{
			int index;
			
			@Override
			public boolean hasNext()
			{
				return this.index < AbstractArraySet.this.size;
			}
			
			@Override
			public E next()
			{
				return (E) AbstractArraySet.this.elements[this.index++];
			}
			
			@Override
			public void remove()
			{
				if (this.index <= 0)
				{
					throw new IllegalStateException();
				}
				AbstractArraySet.this.removeAt(--this.index);
			}
			
			@Override
			public String toString()
			{
				return "SetIterator(" + AbstractArraySet.this + ")";
			}
		};
	}
	
	protected abstract void removeAt(int index);
	
	@Override
	public boolean contains(Object element)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (Objects.equals(this.elements[i], element))
			{
				return true;
			}
		}
		return false;
	}
	
	protected void mapImpl(Function<? super E, ? extends E> mapper)
	{
		int index = 0;
		outer:
		for (int i = 0; i < this.size; i++)
		{
			E newElement = mapper.apply((E) this.elements[i]);
			
			// Search if the mapped element is already present in the array
			for (int j = 0; j < index; j++)
			{
				if (Objects.equals(this.elements[i], newElement))
				{
					continue outer;
				}
			}
			
			this.elements[index++] = newElement;
		}
		
		this.size = index;
	}
	
	protected void flatMapImpl(Function<? super E, ? extends Iterable<? extends E>> mapper)
	{
		Object[] newArray = new Object[this.size << 2];
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			results:
			for (E result : mapper.apply((E) this.elements[i]))
			{
				// Search if the mapped element is already present in the array
				for (int j = 0; j < index; j++)
				{
					if (Objects.equals(newArray[j], result))
					{
						continue results;
					}
				}
				
				// Add the element to the array
				int index1 = index++;
				if (index1 >= newArray.length)
				{
					Object[] temp = new Object[index << 1];
					System.arraycopy(newArray, 0, temp, 0, newArray.length);
					newArray = temp;
				}
				newArray[index1] = result;
			}
		}
		
		this.elements = newArray;
		this.size = index;
	}
	
	@Override
	public void toArray(int index, Object[] store)
	{
		System.arraycopy(this.elements, 0, store, index, this.size);
	}
	
	@Override
	public java.util.Set<E> toJava()
	{
		java.util.LinkedHashSet<E> set = new java.util.LinkedHashSet<>(this.size);
		for (int i = 0; i < this.size; i++)
		{
			set.add((E) this.elements[i]);
		}
		return set;
	}
	
	@Override
	public String toString()
	{
		if (this.size == 0)
		{
			return "[]";
		}
		
		StringBuilder builder = new StringBuilder("[");
		builder.append(this.elements[0]);
		for (int i = 1; i < this.size; i++)
		{
			builder.append(", ").append(this.elements[i]);
		}
		return builder.append("]").toString();
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
