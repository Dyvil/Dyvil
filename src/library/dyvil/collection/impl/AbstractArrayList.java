package dyvil.collection.impl;

import dyvil.collection.Collection;
import dyvil.collection.List;
import dyvil.collection.Set;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public abstract class AbstractArrayList<E> implements List<E>
{
	private static final long serialVersionUID = 5613951730812933112L;
	
	protected static final int DEFAULT_CAPACITY = 10;
	
	protected transient Object[] elements;
	protected transient int      size;
	
	public AbstractArrayList(E... elements)
	{
		this.elements = elements.clone();
		this.size = elements.length;
	}
	
	public AbstractArrayList(E[] elements, int size)
	{
		this.elements = new Object[size];
		System.arraycopy(elements, 0, this.elements, 0, size);
		this.size = size;
	}
	
	public AbstractArrayList(E[] elements, int size, boolean trusted)
	{
		this.elements = elements;
		this.size = size;
	}
	
	public AbstractArrayList(Collection<E> collection)
	{
		this.size = collection.size();
		this.elements = new Object[this.size];
		
		int index = 0;
		for (E element : collection)
		{
			this.elements[index++] = element;
		}
	}
	
	protected void rangeCheck(int index)
	{
		if (index < 0)
		{
			throw new IndexOutOfBoundsException("List Index out of Bounds: " + index + " < 0");
		}
		if (index >= this.size)
		{
			throw new IndexOutOfBoundsException("List Index out of Bounds: " + index + " >= " + this.size);
		}
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
	public boolean isDistinct()
	{
		return Set.isDistinct(this.elements, this.size);
	}
	
	@Override
	public void forEach(Consumer<? super E> action)
	{
		for (int i = 0; i < this.size; i++)
		{
			action.accept((E) this.elements[i]);
		}
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
				return this.index < AbstractArrayList.this.size;
			}
			
			@Override
			public E next()
			{
				return (E) AbstractArrayList.this.elements[this.index++];
			}
			
			@Override
			public void remove()
			{
				if (this.index <= 0)
				{
					throw new IllegalStateException();
				}
				AbstractArrayList.this.removeAt(--this.index);
			}
			
			@Override
			public String toString()
			{
				return "ListIterator(" + AbstractArrayList.this + ")";
			}
		};
	}
	
	@Override
	public Iterator<E> reverseIterator()
	{
		return new Iterator<E>()
		{
			int index = AbstractArrayList.this.size - 1;
			
			@Override
			public boolean hasNext()
			{
				return this.index >= 0;
			}
			
			@Override
			public E next()
			{
				return (E) AbstractArrayList.this.elements[this.index--];
			}
			
			@Override
			public void remove()
			{
				if (this.index >= AbstractArrayList.this.size - 1)
				{
					throw new IllegalStateException();
				}
				AbstractArrayList.this.removeAt(++this.index);
			}
			
			@Override
			public String toString()
			{
				return "ReverseListIterator(" + AbstractArrayList.this + ")";
			}
		};
	}
	
	@Override
	public <R> R foldLeft(R initialValue, BiFunction<? super R, ? super E, ? extends R> reducer)
	{
		for (int i = 0; i < this.size; i++)
		{
			initialValue = reducer.apply(initialValue, (E) this.elements[i]);
		}
		return initialValue;
	}
	
	@Override
	public <R> R foldRight(R initialValue, BiFunction<? super R, ? super E, ? extends R> reducer)
	{
		for (int i = this.size - 1; i >= 0; i--)
		{
			initialValue = reducer.apply(initialValue, (E) this.elements[i]);
		}
		return initialValue;
	}
	
	@Override
	public E reduceLeft(BiFunction<? super E, ? super E, ? extends E> reducer)
	{
		if (this.size == 0)
		{
			return null;
		}
		
		E initialValue = (E) this.elements[0];
		for (int i = 1; i < this.size; i++)
		{
			initialValue = reducer.apply(initialValue, (E) this.elements[i]);
		}
		return initialValue;
	}
	
	@Override
	public E reduceRight(BiFunction<? super E, ? super E, ? extends E> reducer)
	{
		if (this.size == 0)
		{
			return null;
		}
		
		E initialValue = (E) this.elements[this.size - 1];
		for (int i = this.size - 2; i >= 0; i--)
		{
			initialValue = reducer.apply(initialValue, (E) this.elements[i]);
		}
		return initialValue;
	}
	
	@Override
	public boolean contains(Object element)
	{
		if (element == null)
		{
			for (int i = 0; i < this.size; i++)
			{
				if (this.elements[i] == null)
				{
					return true;
				}
			}
			return false;
		}
		
		for (int i = 0; i < this.size; i++)
		{
			if (element.equals(this.elements[i]))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public E subscript(int index)
	{
		this.rangeCheck(index);
		return (E) this.elements[index];
	}
	
	@Override
	public E get(int index)
	{
		if (index < 0 || index >= this.size)
		{
			return null;
		}
		return (E) this.elements[index];
	}
	
	@Override
	public int indexOf(Object element)
	{
		if (element == null)
		{
			for (int i = 0; i < this.size; i++)
			{
				if (this.elements[i] == null)
				{
					return i;
				}
			}
			return -1;
		}
		
		for (int i = 0; i < this.size; i++)
		{
			if (element.equals(this.elements[i]))
			{
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public int lastIndexOf(Object element)
	{
		if (element == null)
		{
			for (int i = this.size - 1; i >= 0; i--)
			{
				if (this.elements[i] == null)
				{
					return i;
				}
			}
			return -1;
		}
		
		for (int i = this.size - 1; i >= 0; i--)
		{
			if (element.equals(this.elements[i]))
			{
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public Object[] toArray()
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		return array;
	}
	
	@Override
	public E[] toArray(Class<E> type)
	{
		E[] array = (E[]) Array.newInstance(type, this.size);
		for (int i = 0; i < this.size; i++)
		{
			array[i] = type.cast(this.elements[i]);
		}
		return array;
	}
	
	@Override
	public void toArray(int index, Object[] store)
	{
		System.arraycopy(this.elements, 0, store, index, this.size);
	}
	
	@Override
	public java.util.List<E> toJava()
	{
		java.util.ArrayList<E> list = new java.util.ArrayList<E>(this.size);
		for (int i = 0; i < this.size; i++)
		{
			list.add((E) this.elements[i]);
		}
		return list;
	}
	
	@Override
	public String toString()
	{
		if (this.size == 0)
		{
			return "[]";
		}
		
		StringBuilder buf = new StringBuilder(this.size * 10).append('[');
		buf.append(this.elements[0]);
		for (int i = 1; i < this.size; i++)
		{
			buf.append(", ");
			buf.append(this.elements[i]);
		}
		buf.append(']');
		return buf.toString();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return List.listEquals(this, obj);
	}
	
	@Override
	public int hashCode()
	{
		return List.listHashCode(this);
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
