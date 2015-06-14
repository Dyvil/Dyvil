package dyvil.collection.mutable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import dyvil.collection.ImmutableList;
import dyvil.collection.MutableList;
import dyvil.collection.impl.AbstractArrayList;
import dyvil.lang.Collection;
import dyvil.lang.Set;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

@NilConvertible
@ArrayConvertible
public class ArrayList<E> extends AbstractArrayList<E> implements MutableList<E>
{
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
		super();
	}
	
	public ArrayList(int size)
	{
		super(size);
	}
	
	public ArrayList(Object... elements)
	{
		super(elements);
	}
	
	public ArrayList(Object[] elements, boolean trusted)
	{
		super(elements, trusted);
	}
	
	public ArrayList(Object[] elements, int size)
	{
		super(elements, size);
	}
	
	public ArrayList(Object[] elements, int size, boolean trusted)
	{
		super(elements, size, trusted);
	}
	
	public ArrayList(Collection<E> collection)
	{
		super(collection);
	}
	
	@Override
	public Iterator<E> iterator()
	{
		return new Iterator<E>()
		{
			int	index;
			
			@Override
			public boolean hasNext()
			{
				return this.index < ArrayList.this.size;
			}
			
			@Override
			public E next()
			{
				return (E) ArrayList.this.elements[this.index++];
			}
			
			@Override
			public void remove()
			{
				if (this.index <= 0)
				{
					throw new IllegalStateException();
				}
				ArrayList.this.removeAt(this.index - 1);
				this.index--;
			}
			
			@Override
			public String toString()
			{
				return "ListIterator(" + ArrayList.this + ")";
			}
		};
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
	public MutableList<E> $minus(Object element)
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
	public MutableList<E> distinct()
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		int size = Set.distinct(array, this.size);
		return new ArrayList(array, size, true);
	}
	
	@Override
	public MutableList<E> distinct(Comparator<? super E> comparator)
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		int size = Set.distinct((E[]) array, this.size, comparator);
		return new ArrayList(array, size, true);
	}
	
	@Override
	public void $plus$eq(E element)
	{
		this.ensureCapacity(this.size + 1);
		this.elements[this.size++] = element;
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
	public void clear()
	{
		for (int i = 0; i < this.size; i++)
		{
			this.elements[i] = null;
		}
		
		this.size = 0;
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
	public boolean addAll(Collection<? extends E> collection)
	{
		if (collection.isEmpty())
		{
			return false;
		}
		
		this.ensureCapacity(this.size + collection.size());
		collection.toArray(this.size, this.elements);
		return true;
	}
	
	@Override
	public boolean remove(E element)
	{
		boolean removed = false;
		for (int index = 0; index < this.size; index++)
		{
			if (Objects.equals(element, this.elements[index]))
			{
				int numMoved = --this.size - index;
				if (numMoved > 0)
				{
					System.arraycopy(this.elements, index + 1, this.elements, index, numMoved);
				}
				index--;
				removed = true;
				this.elements[this.size] = null;
			}
		}
		
		return removed;
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
	public void distinguish()
	{
		this.size = Set.distinct(this.elements, this.size);
	}
	
	@Override
	public void distinguish(Comparator<? super E> comparator)
	{
		this.size = Set.distinct((E[]) this.elements, this.size, comparator);
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
}
