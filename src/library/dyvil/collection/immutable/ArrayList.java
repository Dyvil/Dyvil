package dyvil.collection.immutable;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.collection.ArrayIterator;
import dyvil.collection.mutable.MutableList;
import dyvil.lang.Collection;

public class ArrayList<E> implements ImmutableList<E>
{
	private final Object[]	elements;
	private final int		size;
	
	public ArrayList(Object[] elements)
	{
		this.elements = elements;
		this.size = elements.length;
	}
	
	public ArrayList(Object[] elements, int size)
	{
		this.elements = elements;
		this.size = size;
	}
	
	public ArrayList(Collection<E> elements)
	{
		this.size = elements.size();
		this.elements = new Object[this.size];
		
		int index = 0;
		for (E element : elements)
		{
			this.elements[index++] = element;
		}
	}
	
	private void rangeCheck(int index)
	{
		if (index < 0)
		{
			throw new IndexOutOfBoundsException("ArrayList Index out of Bounds: " + index + " < 0");
		}
		if (index >= this.size)
		{
			throw new IndexOutOfBoundsException("ArrayList Index out of Bounds: " + index + " >= " + this.size);
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
	public Iterator<E> iterator()
	{
		return new ArrayIterator(this.elements, this.size);
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
	public boolean $qmark(Object element)
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
	public E apply(int index)
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
	public ImmutableList<E> slice(int startIndex, int length)
	{
		this.rangeCheck(startIndex);
		if (startIndex + length >= this.size)
		{
			throw new IndexOutOfBoundsException("Array Length out of Bounds: " + length);
		}
		
		Object[] array = new Object[length];
		System.arraycopy(this.elements, startIndex, array, 0, length);
		return new ArrayList(array, length);
	}
	
	@Override
	public ImmutableList<E> $plus(E element)
	{
		Object[] array = new Object[this.size + 1];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		array[this.size] = element;
		return new ArrayList(array, this.size + 1);
	}
	
	@Override
	public ImmutableList<? extends E> $plus$plus(Collection<? extends E> collection)
	{
		int len = collection.size();
		Object[] array = new Object[this.size + len];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		
		Object[] array1 = collection.toArray();
		System.arraycopy(array1, 0, array, this.size, len);
		return new ArrayList(array, this.size + len);
	}
	
	@Override
	public ImmutableList<E> $minus(E element)
	{
		int index = this.indexOf(element);
		if (index < 0)
		{
			return this;
		}
		
		Object[] array = new Object[this.size - 1];
		if (index > 0)
		{
			// copy the first part before the index
			System.arraycopy(this.elements, 0, array, 0, index);
		}
		if (index < this.size)
		{
			// copy the second part after the index
			System.arraycopy(this.elements, index + 1, array, index, this.size - index - 1);
		}
		return new ArrayList(array, this.size - 1);
	}
	
	@Override
	public ImmutableList<? extends E> $minus$minus(Collection<? extends E> collection)
	{
		int index = 0;
		Object[] array = new Object[this.size];
		
		for (int i = 0; i < this.size; i++)
		{
			Object e = this.elements[i];
			if (!collection.$qmark(e))
			{
				array[index++] = e;
			}
		}
		return new ArrayList(array, index);
	}
	
	@Override
	public ImmutableList<? extends E> $amp(Collection<? extends E> collection)
	{
		int index = 0;
		Object[] array = new Object[this.size];
		
		for (int i = 0; i < this.size; i++)
		{
			Object e = this.elements[i];
			if (collection.$qmark(e))
			{
				array[index++] = e;
			}
		}
		return new ArrayList(array, index);
		
	}
	
	@Override
	public <R> ImmutableList<R> mapped(Function<? super E, ? extends R> mapper)
	{
		Object[] array = new Object[this.size];
		for (int i = 0; i < this.size; i++)
		{
			array[i] = mapper.apply((E) this.elements[i]);
		}
		return new ArrayList(array, this.size);
	}
	
	@Override
	public <R> ImmutableList<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		dyvil.collection.mutable.ArrayList<R> list = new dyvil.collection.mutable.ArrayList(this.size << 2);
		for (int i = 0; i < this.size; i++)
		{
			for (R r : mapper.apply((E) this.elements[i]))
			{
				list.$plus$eq(r);
			}
		}
		return list.immutable();
	}
	
	@Override
	public ImmutableList<E> filtered(Predicate<? super E> condition)
	{
		int index = 0;
		Object[] array = new Object[this.size];
		for (int i = 0; i < this.size; i++)
		{
			Object e = this.elements[i];
			if (condition.test((E) e))
			{
				array[index++] = e;
			}
		}
		return new ArrayList(array, index);
	}
	
	@Override
	public ImmutableList<E> sorted()
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		Arrays.sort(array, 0, this.size);
		return new ArrayList(array, this.size);
	}
	
	@Override
	public ImmutableList<E> sorted(Comparator<? super E> comparator)
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		Arrays.sort((E[]) array, 0, this.size, comparator);
		return new ArrayList(array, this.size);
	}
	
	@Override
	public int indexOf(E element)
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
	public int lastIndexOf(E element)
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
	public Object[] toArray(Object[] store)
	{
		if (store.length < this.size)
		{
			return Arrays.copyOf(this.elements, this.size, store.getClass());
		}
		System.arraycopy(this.elements, 0, store, 0, this.size);
		return store;
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
	public ImmutableList<E> copy()
	{
		return new ArrayList(this.elements, this.size);
	}
	
	@Override
	public MutableList<E> mutable()
	{
		// Since this immutable ArrayList does not copy the backing array, we
		// have to copy it in the mutable ArrayList constructor.
		return new dyvil.collection.mutable.ArrayList(this.elements, this.size);
	}
	
	@Override
	public String toString()
	{
		if (this.size == 0)
		{
			return "[ ]";
		}
		
		StringBuilder buf = new StringBuilder("[ ");
		buf.append(this.elements[0]);
		for (int i = 1; i < this.size; i++)
		{
			buf.append(", ");
			buf.append(this.elements[i]);
		}
		buf.append(" ]");
		return buf.toString();
	}
}
