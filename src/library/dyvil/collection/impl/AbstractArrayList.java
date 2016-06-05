package dyvil.collection.impl;

import dyvil.collection.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
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

	public AbstractArrayList()
	{
		this.elements = new Object[DEFAULT_CAPACITY];
	}

	public AbstractArrayList(int capacity)
	{
		this.elements = new Object[capacity];
	}

	public AbstractArrayList(E[] elements)
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

	public AbstractArrayList(E[] elements, int size, @SuppressWarnings("UnusedParameters") boolean trusted)
	{
		this.elements = elements;
		this.size = size;
	}

	public AbstractArrayList(Iterable<? extends E> iterable)
	{
		this();
		this.addAllInternal(iterable);
	}

	public AbstractArrayList(Collection<? extends E> collection)
	{
		this(collection.size());
		this.addAllInternal(collection);
	}

	public AbstractArrayList(AbstractArrayList<? extends E> arrayList)
	{
		this.size = arrayList.size;
		this.elements = arrayList.elements.clone();
	}

	protected void addInternal(E element)
	{
		final int index = this.size;
		this.ensureCapacityInternal(index + 1);
		this.elements[index] = element;
		this.size++;
	}

	protected void addAllInternal(Iterable<? extends E> iterable)
	{
		for (E element : iterable)
		{
			this.addInternal(element);
		}
	}

	protected void addAllInternal(Collection<? extends E> collection)
	{
		this.ensureCapacityInternal(this.size + collection.size());
		collection.toArray(this.size, this.elements);
	}

	protected void ensureCapacityInternal(int minCapacity)
	{
		final int length = this.elements.length;
		if (minCapacity - length <= 0)
		{
			return;
		}

		int newCapacity = length + (length >> 1);
		if (newCapacity - minCapacity < 0)
		{
			newCapacity = minCapacity;
		}
		this.elements = Arrays.copyOf(this.elements, newCapacity);
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
	public E get(int index)
	{
		List.rangeCheck(index, this.size);
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
	public <R> MutableList<R> emptyCopy()
	{
		return new dyvil.collection.mutable.ArrayList<>();
	}

	@Override
	public <R> MutableList<R> emptyCopy(int newCapacity)
	{
		return new dyvil.collection.mutable.ArrayList<>(newCapacity);
	}

	@Override
	public MutableList<E> mutableCopy()
	{
		return new dyvil.collection.mutable.ArrayList<>((E[]) this.elements, this.size);
	}

	@Override
	public ImmutableList<E> immutableCopy()
	{
		return new dyvil.collection.immutable.ArrayList<>((E[]) this.elements, this.size);
	}

	@Override
	public <RE> ImmutableList.Builder<RE> immutableBuilder()
	{
		return dyvil.collection.immutable.ArrayList.builder();
	}

	@Override
	public <RE> ImmutableList.Builder<RE> immutableBuilder(int capacity)
	{
		return dyvil.collection.immutable.ArrayList.builder(capacity);
	}

	@Override
	public java.util.List<E> toJava()
	{
		java.util.ArrayList<E> list = new java.util.ArrayList<>(this.size);
		for (int i = 0; i < this.size; i++)
		{
			list.add((E) this.elements[i]);
		}
		return list;
	}

	@Override
	public String toString()
	{
		if (this.size <= 0)
		{
			return Collection.EMPTY_STRING;
		}

		final StringBuilder builder = new StringBuilder(this.size << 3).append(Collection.START_STRING);
		builder.append(this.elements[0]);
		for (int i = 1; i < this.size; i++)
		{
			builder.append(Collection.ELEMENT_SEPARATOR_STRING);
			builder.append(this.elements[i]);
		}
		return builder.append(Collection.END_STRING).toString();
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
