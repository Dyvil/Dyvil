package dyvil.collection.mutable;

import dyvil.collection.Collection;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.Set;
import dyvil.collection.impl.AbstractArraySet;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import java.util.Objects;
import java.util.function.Function;

@NilConvertible
@ArrayConvertible
public class ArraySet<E> extends AbstractArraySet<E> implements MutableSet<E>
{
	private static final long serialVersionUID = -6676561653968567088L;

	// Factory Methods

	public static <E> ArraySet<E> apply()
	{
		return new ArraySet<>();
	}

	@SafeVarargs
	public static <E> ArraySet<E> apply(E... elements)
	{
		return new ArraySet<>(elements, true);
	}

	public static <E> ArraySet<E> from(E[] array)
	{
		return new ArraySet<>(array);
	}

	public static <E> ArraySet<E> from(Iterable<? extends E> iterable)
	{
		return new ArraySet<>(iterable);
	}

	public static <E> ArraySet<E> from(Collection<? extends E> collection)
	{
		return new ArraySet<>(collection);
	}

	public static <E> ArraySet<E> from(Set<? extends E> set)
	{
		return new ArraySet<>(set);
	}

	public static <E> ArraySet<E> from(AbstractArraySet<? extends E> arraySet)
	{
		return new ArraySet<>(arraySet);
	}

	// Constructors

	public ArraySet()
	{
		super();
	}

	public ArraySet(int capacity)
	{
		super(capacity);
	}

	public ArraySet(E[] elements)
	{
		super(elements);
	}

	public ArraySet(E[] elements, int size)
	{
		super(elements, size);
	}

	public ArraySet(E[] elements, boolean trusted)
	{
		super(elements, trusted);
	}

	public ArraySet(E[] elements, int size, boolean trusted)
	{
		super(elements, size, trusted);
	}

	public ArraySet(Iterable<? extends E> iterable)
	{
		super(iterable);
	}

	public ArraySet(Collection<? extends E> collection)
	{
		super(collection);
	}

	public ArraySet(Set<? extends E> set)
	{
		super(set);
	}

	public ArraySet(AbstractArraySet<? extends E> arraySet)
	{
		super(arraySet);
	}

	// Implementation Methods

	@Override
	public void clear()
	{
		this.size = 0;
		for (int i = 0; i < this.elements.length; i++)
		{
			this.elements[i] = null;
		}
	}

	@Override
	public boolean add(E element)
	{
		return this.addInternal(element);
	}

	@Override
	public boolean remove(Object element)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (Objects.equals(this.elements[i], element))
			{
				this.removeAt(i);
				return true;
			}
		}
		return false;
	}

	@Override
	protected void removeAt(int index)
	{
		int numMoved = --this.size - index;
		if (numMoved > 0)
		{
			System.arraycopy(this.elements, index + 1, this.elements, index, numMoved);
		}
		this.elements[this.size] = null;
	}

	@Override
	public void map(Function<? super E, ? extends E> mapper)
	{
		this.mapImpl(mapper);
	}

	@Override
	public void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper)
	{
		this.flatMapImpl(mapper);
	}

	@Override
	public MutableSet<E> copy()
	{
		return this.mutableCopy();
	}

	@Override
	public ImmutableSet<E> immutable()
	{
		return this.immutableCopy();
	}
}
