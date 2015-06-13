package dyvil.collection.immutable;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.iterator.SingletonIterator;
import dyvil.lang.Collection;
import dyvil.lang.Set;
import dyvil.lang.literal.TupleConvertible;

@TupleConvertible
public class SingletonSet<E> implements ImmutableSet<E>
{
	private E	element;
	
	public static <E> SingletonSet<E> apply(E element)
	{
		return new SingletonSet(element);
	}
	
	public SingletonSet(E element)
	{
		this.element = element;
	}
	
	@Override
	public int size()
	{
		return 1;
	}
	
	@Override
	public boolean isEmpty()
	{
		return false;
	}
	
	@Override
	public Iterator<E> iterator()
	{
		return new SingletonIterator<E>(this.element);
	}
	
	@Override
	public void forEach(Consumer<? super E> action)
	{
		action.accept(this.element);
	}
	
	@Override
	public boolean $qmark(Object element)
	{
		return Objects.equals(element, this.element);
	}
	
	@Override
	public ImmutableSet<E> $plus(E element)
	{
		return ImmutableSet.apply(this.element, element);
	}
	
	@Override
	public ImmutableSet<E> $minus(Object element)
	{
		if (Objects.equals(this.element, element))
		{
			return ImmutableSet.apply();
		}
		return ImmutableSet.apply(this.element);
	}
	
	@Override
	public ImmutableSet<? extends E> $minus$minus(Collection<? extends E> collection)
	{
		if (collection.$qmark(this.element))
		{
			return ImmutableSet.apply();
		}
		return ImmutableSet.apply(this.element);
	}
	
	@Override
	public ImmutableSet<? extends E> $amp(Collection<? extends E> collection)
	{
		if (!collection.$qmark(this.element))
		{
			return ImmutableSet.apply();
		}
		return this;
	}
	
	@Override
	public ImmutableSet<? extends E> $bar(Collection<? extends E> collection)
	{
		if (!collection.$qmark(this))
		{
			return new ArraySet(collection);
		}
		
		Object[] array = new Object[1 + collection.size()];
		int index = 1;
		array[0] = this.element;
		outer:
		for (E element : collection)
		{
			for (int i = 1; i < index; i++)
			{
				if (Objects.equals(array[i], element))
				{
					continue outer;
				}
			}
			
			array[index++] = element;
		}
		return new ArraySet(array, index, true);
	}
	
	@Override
	public ImmutableSet<? extends E> $up(Collection<? extends E> collection)
	{
		if (!collection.$qmark(this.element))
		{
			return new ArraySet(collection);
		}
		
		Object[] array = new Object[1 + collection.size()];
		int index = 1;
		array[0] = this.element;
		outer:
		for (E element : collection)
		{
			for (int i = 1; i < index; i++)
			{
				if (Objects.equals(array[i], element))
				{
					continue outer;
				}
			}
			
			array[index++] = element;
		}
		return new ArraySet(array, index, true);
	}
	
	@Override
	public <R> ImmutableSet<R> mapped(Function<? super E, ? extends R> mapper)
	{
		return ImmutableSet.apply(mapper.apply(this.element));
	}
	
	@Override
	public <R> ImmutableSet<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		return (ImmutableSet<R>) ImmutableSet.apply(mapper.apply(this.element));
	}
	
	@Override
	public ImmutableSet<E> filtered(Predicate<? super E> condition)
	{
		if (condition.test(this.element))
		{
			return ImmutableSet.apply(this.element);
		}
		return ImmutableSet.apply();
	}
	
	@Override
	public Object[] toArray()
	{
		return new Object[] { this.element };
	}
	
	@Override
	public E[] toArray(Class<E> type)
	{
		E[] array = (E[]) Array.newInstance(type, 1);
		array[0] = type.cast(this.element);
		return array;
	}
	
	@Override
	public void toArray(int index, Object[] store)
	{
		store[index] = this.element;
	}
	
	@Override
	public ImmutableSet<E> copy()
	{
		return ImmutableSet.apply(this.element);
	}
	
	@Override
	public MutableSet<E> mutable()
	{
		return MutableSet.apply(this.element);
	}
	
	@Override
	public String toString()
	{
		return new StringBuilder().append('[').append(this.element).append(']').toString();
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
}
