package dyvil.collection.impl;

import dyvil.collection.Collection;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.Set;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

public abstract class AbstractArraySet<E> implements Set<E>
{
	private static final long serialVersionUID = -7004392809193010314L;
	
	protected static final int DEFAULT_CAPACITY = 10;
	
	protected transient Object[] elements;
	protected transient int      size;
	
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
	
	public AbstractArraySet(Object[] elements, int size, @SuppressWarnings("UnusedParameters") boolean trusted)
	{
		this.elements = elements;
		this.size = size;
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

	protected void addInternal(E element)
	{

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
		return Collection.isSorted(this.elements, this.size);
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
	public <R> MutableSet<R> emptyCopy()
	{
		return new dyvil.collection.mutable.ArraySet<>();
	}

	@Override
	public <RE> MutableSet<RE> emptyCopy(int capacity)
	{
		return new dyvil.collection.mutable.ArraySet<>(capacity);
	}

	@Override
	public MutableSet<E> mutableCopy()
	{
		return new dyvil.collection.mutable.ArraySet<>((E[]) this.elements, this.size);
	}

	@Override
	public ImmutableSet<E> immutableCopy()
	{
		return new dyvil.collection.immutable.ArraySet<>((E[]) this.elements, this.size);
	}

	@Override
	public <RE> ImmutableSet.Builder<RE> immutableBuilder()
	{
		return dyvil.collection.immutable.ArraySet.builder();
	}

	@Override
	public <RE> ImmutableSet.Builder<RE> immutableBuilder(int capacity)
	{
		return dyvil.collection.immutable.ArraySet.builder(capacity);
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
			return Collection.EMPTY_STRING;
		}
		
		final StringBuilder builder = new StringBuilder(this.size << 3).append(Collection.START_STRING);
		builder.append(this.elements[0]);
		for (int i = 1; i < this.size; i++)
		{
			builder.append(Collection.ELEMENT_SEPARATOR_STRING).append(this.elements[i]);
		}
		return builder.append(Collection.END_STRING).toString();
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
		
		out.writeInt(this.size);
		for (int i = 0; i < this.size; i++)
		{
			out.writeObject(this.elements[i]);
		}
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		
		this.size = in.readInt();
		this.elements = new Object[this.size];
		for (int i = 0; i < this.size; i++)
		{
			this.elements[i] = in.readObject();
		}
	}
}
