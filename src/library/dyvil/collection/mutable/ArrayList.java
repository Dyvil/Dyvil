package dyvil.collection.mutable;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import dyvil.collection.ArrayIterator;
import dyvil.collection.ImmutableList;
import dyvil.collection.MutableList;
import dyvil.lang.Collection;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

@NilConvertible
@ArrayConvertible
public class ArrayList<E> implements MutableList<E>
{
	private static final int	INITIAL_CAPACITY	= 10;
	
	private Object[]			elements;
	private int					size;
	
	public static <E> ArrayList<E> apply()
	{
		return new ArrayList();
	}
	
	public static <E> ArrayList<E> apply(E... elements)
	{
		return new ArrayList(elements, true);
	}
	
	public ArrayList()
	{
		this.elements = new Object[INITIAL_CAPACITY];
	}
	
	public ArrayList(int size)
	{
		this.elements = new Object[size];
	}
	
	public ArrayList(Object... elements)
	{
		this.elements = new Object[elements.length];
		System.arraycopy(elements, 0, this.elements, 0, elements.length);
		this.size = elements.length;
	}
	
	public ArrayList(Object[] elements, int size)
	{
		this.elements = new Object[size];
		System.arraycopy(elements, 0, this.elements, 0, size);
		this.size = size;
	}
	
	public ArrayList(Object[] elements, boolean trusted)
	{
		this.elements = elements;
		this.size = elements.length;
	}
	
	public ArrayList(Object[] elements, int size, boolean trusted)
	{
		this.elements = elements;
		this.size = size;
	}
	
	public ArrayList(Collection<E> collection)
	{
		this.size = collection.size();
		this.elements = new Object[this.size];
		
		int index = 0;
		for (E element : collection)
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
	public MutableList<E> subList(int startIndex, int length)
	{
		this.rangeCheck(startIndex);
		if (startIndex + length >= this.size)
		{
			throw new IndexOutOfBoundsException("Array Length out of Bounds: " + length);
		}
		
		Object[] array = new Object[length];
		System.arraycopy(this.elements, startIndex, array, 0, length);
		return new ArrayList(array, length, true);
	}
	
	@Override
	public MutableList<E> $plus(E element)
	{
		Object[] array = new Object[this.size + 1];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		array[this.size] = element;
		return new ArrayList(array, this.size + 1, true);
	}
	
	@Override
	public MutableList<? extends E> $plus$plus(Collection<? extends E> collection)
	{
		int len = collection.size();
		Object[] array = new Object[this.size + len];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		
		Object[] array1 = collection.toArray();
		System.arraycopy(array1, 0, array, this.size, len);
		return new ArrayList(array, this.size + len, true);
	}
	
	@Override
	public MutableList<E> $minus(E element)
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
		return new ArrayList(array, this.size - 1, true);
	}
	
	@Override
	public MutableList<? extends E> $minus$minus(Collection<? extends E> collection)
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
		return new ArrayList(array, index, true);
	}
	
	@Override
	public MutableList<? extends E> $amp(Collection<? extends E> collection)
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
		return new ArrayList(array, index, true);
		
	}
	
	@Override
	public <R> MutableList<R> mapped(Function<? super E, ? extends R> mapper)
	{
		Object[] array = new Object[this.size];
		for (int i = 0; i < this.size; i++)
		{
			array[i] = mapper.apply((E) this.elements[i]);
		}
		return new ArrayList(array, this.size, true);
	}
	
	@Override
	public <R> MutableList<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		ArrayList<R> list = new ArrayList(this.size << 2);
		for (int i = 0; i < this.size; i++)
		{
			for (R r : mapper.apply((E) this.elements[i]))
			{
				list.$plus$eq(r);
			}
		}
		return list;
	}
	
	@Override
	public MutableList<E> filtered(Predicate<? super E> condition)
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
		return new ArrayList(array, index, true);
	}
	
	@Override
	public MutableList<E> sorted()
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		Arrays.sort(array, 0, this.size);
		return new ArrayList(array, this.size, true);
	}
	
	@Override
	public MutableList<E> sorted(Comparator<? super E> comparator)
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		Arrays.sort((E[]) array, 0, this.size, comparator);
		return new ArrayList(array, this.size, true);
	}
	
	@Override
	public void resize(int newLength)
	{
		if (newLength < this.size)
		{
			for (int i = newLength; i < this.size; i++)
			{
				this.elements[i] = null;
			}
			this.size = newLength;
			return;
		}
		
		if (newLength > this.elements.length)
		{
			Object[] temp = new Object[newLength];
			System.arraycopy(this.elements, 0, temp, 0, this.size);
			this.elements = temp;
		}
		this.size = newLength;
	}
	
	@Override
	public void ensureCapacity(int minSize)
	{
		if (minSize > this.elements.length)
		{
			Object[] temp = new Object[minSize];
			System.arraycopy(this.elements, 0, temp, 0, this.size);
			this.elements = temp;
		}
	}
	
	@Override
	public void update(int index, E element)
	{
		this.rangeCheck(index);
		this.elements[index] = element;
	}
	
	@Override
	public E set(int index, E element)
	{
		if (index < 0)
		{
			return null;
		}
		if (index >= this.size)
		{
			this.resize(index + 1);
		}
		
		E e = (E) this.elements[index];
		this.elements[index] = element;
		return e;
	}
	
	@Override
	public E add(E element)
	{
		this.$plus$eq(element);
		return null;
	}
	
	@Override
	public void insert(int index, E element)
	{
		if (index == this.size)
		{
			this.$plus$eq(element);
			return;
		}
		this.rangeCheck(index);
		
		this.ensureCapacity(this.size + 1);
		System.arraycopy(this.elements, index, this.elements, index + 1, this.size - index);
		this.elements[index] = element;
		this.size++;
	}
	
	@Override
	public E add(int index, E element)
	{
		if (index > this.size)
		{
			return this.set(index, element);
		}
		if (index == this.size)
		{
			this.$plus$eq(element);
			return null;
		}
		
		E e = (E) this.elements[index];
		
		this.resize(index + 1);
		System.arraycopy(this.elements, index, this.elements, index + 1, this.size - index);
		this.elements[index] = element;
		return e;
	}
	
	@Override
	public boolean remove(E element)
	{
		int index = this.indexOf(element);
		if (index < 0)
		{
			return false;
		}
		
		int numMoved = --this.size - index;
		if (numMoved > 0)
		{
			System.arraycopy(this.elements, index + 1, this.elements, index, numMoved);
		}
		this.elements[this.size] = null;
		return true;
	}
	
	@Override
	public void removeAt(int index)
	{
		this.rangeCheck(index);
		int numMoved = --this.size - index;
		if (numMoved > 0)
		{
			System.arraycopy(this.elements, index + 1, this.elements, index, numMoved);
		}
		this.elements[this.size] = null;
	}
	
	@Override
	public void $plus$eq(E element)
	{
		this.ensureCapacity(this.size + 1);
		this.elements[this.size++] = element;
	}
	
	@Override
	public void $plus$plus$eq(Collection<? extends E> collection)
	{
		int len = collection.size();
		this.ensureCapacity(this.size + len);
		Object[] array = collection.toArray();
		System.arraycopy(array, 0, this.elements, this.size, len);
		this.size += len;
	}
	
	@Override
	public void $minus$eq(E element)
	{
		int index = this.indexOf(element);
		if (index < 0)
		{
			return;
		}
		
		int numMoved = --this.size - index;
		if (numMoved > 0)
		{
			System.arraycopy(this.elements, index + 1, this.elements, index, numMoved);
		}
		this.elements[this.size] = null;
	}
	
	@Override
	public void $minus$minus$eq(Collection<? extends E> collection)
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
		this.elements = array;
		this.size = index;
	}
	
	@Override
	public void $amp$eq(Collection<? extends E> collection)
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
		this.elements = array;
		this.size = index;
	}
	
	@Override
	public void clear()
	{
		for (int i = 0; i < this.size; i++)
		{
			this.elements[i] = null;
		}
		
		this.size = 0;
	}
	
	@Override
	public void filter(Predicate<? super E> condition)
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
		this.elements = array;
		this.size = index;
	}
	
	@Override
	public void map(UnaryOperator<E> mapper)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.elements[i] = mapper.apply((E) this.elements[i]);
		}
	}
	
	@Override
	public void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper)
	{
		Object[] array = new Object[this.size << 2];
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			for (E e : mapper.apply((E) this.elements[i]))
			{
				if (index >= array.length)
				{
					Object[] temp = new Object[index + 5];
					System.arraycopy(array, 0, temp, 0, index);
					array = temp;
				}
				array[index++] = e;
			}
		}
		
		this.elements = array;
		this.size = index;
	}
	
	@Override
	public void sort()
	{
		Arrays.sort(this.elements, 0, this.size);
	}
	
	@Override
	public void sort(Comparator<? super E> comparator)
	{
		Arrays.sort((E[]) this.elements, 0, this.size, comparator);
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
	public void toArray(Object[] store)
	{
		System.arraycopy(this.elements, 0, store, 0, this.size);
	}

	@Override
	public MutableList<E> copy()
	{
		return new ArrayList(this.elements, this.size);
	}
	
	@Override
	public ImmutableList<E> immutable()
	{
		return new dyvil.collection.immutable.ArrayList(this.elements, this.size);
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
}
