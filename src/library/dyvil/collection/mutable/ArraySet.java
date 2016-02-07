package dyvil.collection.mutable;

import dyvil.collection.*;
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
	
	public static <E> ArraySet<E> apply()
	{
		return new ArraySet<>();
	}
	
	@SafeVarargs
	public static <E> ArraySet<E> apply(E... elements)
	{
		return new ArraySet<>(elements, true);
	}

	public static <E> ArraySet<E> fromArray(E[] elements)
	{
		return new ArraySet<>(elements);
	}
	
	public ArraySet()
	{
		super(new Object[DEFAULT_CAPACITY], 0, true);
	}
	
	public ArraySet(int size)
	{
		super(new Object[size], 0, true);
	}
	
	@SafeVarargs
	public ArraySet(E... elements)
	{
		super(elements);
	}
	
	public ArraySet(E[] elements, int size)
	{
		super(elements, size);
	}
	
	public ArraySet(E[] elements, boolean trusted)
	{
		super(elements, elements.length, trusted);
	}
	
	public ArraySet(E[] elements, int size, boolean trusted)
	{
		super(elements, size, trusted);
	}
	
	public ArraySet(Collection<E> elements)
	{
		super(elements);
	}
	
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
		if (this.contains(element))
		{
			return false;
		}
		
		int index = this.size++;
		if (index >= this.elements.length)
		{
			Object[] temp = new Object[this.size];
			System.arraycopy(this.elements, 0, temp, 0, index);
			this.elements = temp;
		}
		this.elements[index] = element;
		return true;
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
