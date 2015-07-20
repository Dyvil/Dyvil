package dyvil.collection.immutable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.lang.literal.ArrayConvertible;

import dyvil.collection.Collection;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.impl.AbstractArraySet;
import dyvil.util.ImmutableException;

@ArrayConvertible
public class ArraySet<E> extends AbstractArraySet<E> implements ImmutableSet<E>
{
	public static <E> ArraySet<E> apply(E... elements)
	{
		return new ArraySet(elements);
	}
	
	public static <E> Builder<E> builder()
	{
		return new Builder();
	}
	
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
	
	public static class Builder<E> implements ImmutableSet.Builder<E>
	{
		private Object[]	elements;
		private int			size;
		
		@Override
		public void add(E element)
		{
			if (this.size < 0)
			{
				throw new IllegalStateException("Already built");
			}
			
			for (int i = 0; i < this.size; i++)
			{
				if (Objects.equals(this.elements[i], element))
				{
					this.elements[i] = element;
					return;
				}
			}
			
			int index = this.size++;
			if (index >= this.elements.length)
			{
				Object[] temp = new Object[(int) (this.size * 1.1F)];
				System.arraycopy(this.elements, 0, temp, 0, index);
				this.elements = temp;
			}
			this.elements[index] = element;
		}
		
		@Override
		public ArraySet<E> build()
		{
			ArraySet<E> set = new ArraySet(this.elements, this.size, true);
			this.size = -1;
			return set;
		}
	}
	
	@Override
	protected void removeAt(int index)
	{
		throw new ImmutableException("removeAt() on Immutable Set");
	}
	
	@Override
	public ImmutableSet<E> $plus(E element)
	{
		if (this.contains(element))
		{
			return this;
		}
		
		Object[] newArray = new Object[this.size + 1];
		System.arraycopy(this.elements, 0, newArray, 0, this.size);
		newArray[this.size] = element;
		return new ArraySet(newArray, this.size + 1, true);
	}
	
	@Override
	public ImmutableSet<E> $minus(Object element)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (Objects.equals(this.elements[i], element))
			{
				Object[] newArray = new Object[this.size - 1];
				System.arraycopy(this.elements, 0, newArray, 0, i);
				System.arraycopy(this.elements, i + 1, newArray, i, this.size - i - 1);
				return new ArraySet(newArray, this.size - 1, true);
			}
		}
		return this;
	}
	
	@Override
	public ImmutableSet<? extends E> $minus$minus(Collection<?> collection)
	{
		Object[] newArray = new Object[this.size];
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			E element = (E) this.elements[i];
			if (!collection.contains(element))
			{
				newArray[index++] = element;
			}
		}
		return new ArraySet(newArray, index, true);
	}
	
	@Override
	public ImmutableSet<? extends E> $amp(Collection<? extends E> collection)
	{
		Object[] newArray = new Object[Math.min(this.size, collection.size())];
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			E element = (E) this.elements[i];
			if (collection.contains(element))
			{
				newArray[index++] = element;
			}
		}
		return new ArraySet(newArray, index, true);
	}
	
	@Override
	public ImmutableSet<? extends E> $bar(Collection<? extends E> collection)
	{
		int size = this.size;
		Object[] newArray = new Object[size + collection.size()];
		System.arraycopy(this.elements, 0, newArray, 0, this.size);
		for (E element : collection)
		{
			if (!this.contains(element))
			{
				newArray[size++] = element;
			}
		}
		return new ArraySet(newArray, size, true);
	}
	
	@Override
	public ImmutableSet<? extends E> $up(Collection<? extends E> collection)
	{
		Object[] newArray = new Object[this.size + collection.size()];
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			Object element = this.elements[i];
			if (!collection.contains(element))
			{
				newArray[index++] = element;
			}
		}
		for (E element : collection)
		{
			if (!this.contains(element))
			{
				newArray[index++] = element;
			}
		}
		return new ArraySet(newArray, index, true);
	}
	
	@Override
	public <R> ImmutableSet<R> mapped(Function<? super E, ? extends R> mapper)
	{
		ArraySet<R> copy = (ArraySet<R>) this.copy();
		copy.mapImpl((Function) mapper);
		return copy;
	}
	
	@Override
	public <R> ImmutableSet<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		ArraySet<R> copy = (ArraySet<R>) this.copy();
		copy.flatMapImpl((Function) mapper);
		return copy;
	}
	
	@Override
	public ImmutableSet<E> filtered(Predicate<? super E> condition)
	{
		Object[] newArray = new Object[this.size];
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			Object element = this.elements[i];
			if (condition.test((E) element))
			{
				newArray[index++] = element;
			}
		}
		return new ArraySet(newArray, index, true);
	}
	
	@Override
	public ImmutableSet<E> copy()
	{
		return new ArraySet(this.elements, this.size, true);
	}
	
	@Override
	public MutableSet<E> mutable()
	{
		return new dyvil.collection.mutable.ArraySet<E>((E[]) this.elements, this.size);
	}
}
