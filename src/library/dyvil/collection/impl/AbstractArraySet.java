package dyvil.collection.impl;

import dyvil.annotation.internal.NonNull;
import dyvil.collection.Collection;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.Set;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

public abstract class AbstractArraySet<E> implements Set<E>
{
	private static final long serialVersionUID = -7004392809193010314L;

	protected static final int DEFAULT_CAPACITY = 16;

	protected transient Object[] elements;
	protected transient int      size;

	protected AbstractArraySet()
	{
		this.elements = new Object[DEFAULT_CAPACITY];
	}

	protected AbstractArraySet(int capacity)
	{
		this.elements = new Object[capacity];
	}

	@SafeVarargs
	public AbstractArraySet(@NonNull E... elements)
	{
		this.elements = elements.clone();
		this.size = Set.distinct(this.elements, elements.length);
	}

	public AbstractArraySet(E @NonNull [] elements, int size)
	{
		this.elements = new Object[size];
		System.arraycopy(elements, 0, this.elements, 0, size);
		this.size = Set.distinct(this.elements, size);
	}

	public AbstractArraySet(E @NonNull [] elements, @SuppressWarnings("UnusedParameters") boolean trusted)
	{
		this.elements = elements;
		this.size = Set.distinct(elements, elements.length);
	}

	public AbstractArraySet(E[] elements, int size, @SuppressWarnings("UnusedParameters") boolean trusted)
	{
		this.elements = elements;
		this.size = size;
	}

	public AbstractArraySet(@NonNull Iterable<? extends E> iterable)
	{
		this();
		this.addAllInternal(iterable);
	}

	public AbstractArraySet(@NonNull Collection<? extends E> collection)
	{
		this(collection.size());
		this.addAllInternal(collection);
	}

	public AbstractArraySet(@NonNull Set<? extends E> set)
	{
		this(set.size());
		this.addAllInternal(set);
	}

	public AbstractArraySet(@NonNull AbstractArraySet<? extends E> arraySet)
	{
		this.elements = arraySet.elements.clone();
		this.size = arraySet.size;
	}

	protected boolean addInternal(E element)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (Objects.equals(this.elements[i], element))
			{
				return false;
			}
		}

		final int index = this.size;
		this.ensureCapacityInternal(index + 1);
		this.elements[index] = element;
		this.size++;
		return true;
	}

	protected void addAllInternal(@NonNull Iterable<? extends E> iterable)
	{
		for (E element : iterable)
		{
			this.addInternal(element);
		}
	}

	protected void addAllInternal(@NonNull Set<? extends E> set)
	{
		this.ensureCapacityInternal(this.size + set.size());
		set.toArray(this.size, this.elements);
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
	public boolean isSorted(@NonNull Comparator<? super E> comparator)
	{
		return Collection.isSorted((E[]) this.elements, this.size, comparator);
	}

	@NonNull
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

			@NonNull
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

			@NonNull
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

	protected void mapImpl(@NonNull Function<? super E, ? extends E> mapper)
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

	protected void flatMapImpl(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends E>> mapper)
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
	public void toArray(int index, @NonNull Object @NonNull [] store)
	{
		System.arraycopy(this.elements, 0, store, index, this.size);
	}

	@NonNull
	@Override
	public <R> MutableSet<R> emptyCopy()
	{
		return new dyvil.collection.mutable.ArraySet<>();
	}

	@NonNull
	@Override
	public <RE> MutableSet<RE> emptyCopy(int capacity)
	{
		return new dyvil.collection.mutable.ArraySet<>(capacity);
	}

	@NonNull
	@Override
	public MutableSet<E> mutableCopy()
	{
		return new dyvil.collection.mutable.ArraySet<>((E[]) this.elements, this.size);
	}

	@NonNull
	@Override
	public ImmutableSet<E> immutableCopy()
	{
		return new dyvil.collection.immutable.ArraySet<>((E[]) this.elements, this.size);
	}

	@Override
	public <RE> ImmutableSet.@NonNull Builder<RE> immutableBuilder()
	{
		return dyvil.collection.immutable.ArraySet.builder();
	}

	@Override
	public <RE> ImmutableSet.@NonNull Builder<RE> immutableBuilder(int capacity)
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

	@NonNull
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

	private void writeObject(java.io.@NonNull ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();

		out.writeInt(this.size);
		for (int i = 0; i < this.size; i++)
		{
			out.writeObject(this.elements[i]);
		}
	}

	private void readObject(java.io.@NonNull ObjectInputStream in) throws IOException, ClassNotFoundException
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
