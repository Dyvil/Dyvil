package dyvil.collection.immutable;

import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.lang.literal.TupleConvertible;

import dyvil.collection.Collection;
import dyvil.collection.ImmutableList;
import dyvil.collection.List;
import dyvil.collection.MutableList;
import dyvil.collection.iterator.SingletonIterator;

@TupleConvertible
public class SingletonList<E> implements ImmutableList<E>
{
	private E	element;
	
	public static <E> SingletonList<E> apply(E element)
	{
		return new SingletonList(element);
	}
	
	public SingletonList(E element)
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
	public boolean contains(Object element)
	{
		return Objects.equals(element, this.element);
	}
	
	@Override
	public E subscript(int index)
	{
		if (index == 0)
		{
			return this.element;
		}
		throw new IndexOutOfBoundsException("Index out of bounds for Singleton List");
	}
	
	@Override
	public E get(int index)
	{
		return index == 0 ? this.element : null;
	}
	
	@Override
	public ImmutableList<E> subList(int startIndex, int length)
	{
		if (startIndex > 0 || length > 0)
		{
			throw new IndexOutOfBoundsException("Slice out of range for Empty List");
		}
		return this;
	}
	
	@Override
	public int indexOf(Object element)
	{
		return Objects.equals(this.element, element) ? 0 : -1;
	}
	
	@Override
	public int lastIndexOf(Object element)
	{
		return Objects.equals(this.element, element) ? 0 : -1;
	}
	
	@Override
	public ImmutableList<E> $plus(E element)
	{
		return ImmutableList.apply(this.element, element);
	}
	
	@Override
	public ImmutableList<? extends E> $plus$plus(Collection<? extends E> collection)
	{
		int len = 1 + collection.size();
		Object[] array = new Object[len];
		array[0] = this.element;
		collection.toArray(1, array);
		return new ArrayList(array, len, true);
	}
	
	@Override
	public ImmutableList<E> $minus(Object element)
	{
		if (Objects.equals(this.element, element))
		{
			return ImmutableList.apply();
		}
		return ImmutableList.apply(this.element);
	}
	
	@Override
	public ImmutableList<? extends E> $minus$minus(Collection<?> collection)
	{
		if (collection.contains(this.element))
		{
			return ImmutableList.apply();
		}
		return ImmutableList.apply(this.element);
	}
	
	@Override
	public ImmutableList<? extends E> $amp(Collection<? extends E> collection)
	{
		if (!collection.contains(this.element))
		{
			return ImmutableList.apply();
		}
		return this;
	}
	
	@Override
	public <R> ImmutableList<R> mapped(Function<? super E, ? extends R> mapper)
	{
		return ImmutableList.apply(mapper.apply(this.element));
	}
	
	@Override
	public <R> ImmutableList<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		return (ImmutableList<R>) ImmutableList.apply(mapper.apply(this.element));
	}
	
	@Override
	public ImmutableList<E> filtered(Predicate<? super E> condition)
	{
		if (condition.test(this.element))
		{
			return ImmutableList.apply(this.element);
		}
		return ImmutableList.apply();
	}
	
	@Override
	public ImmutableList<E> sorted()
	{
		return ImmutableList.apply(this.element);
	}
	
	@Override
	public ImmutableList<E> sorted(Comparator<? super E> comparator)
	{
		return ImmutableList.apply(this.element);
	}
	
	@Override
	public ImmutableList<E> distinct()
	{
		return ImmutableList.apply(this.element);
	}
	
	@Override
	public ImmutableList<E> distinct(Comparator<? super E> comparator)
	{
		return ImmutableList.apply(this.element);
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
	public ImmutableList<E> copy()
	{
		return ImmutableList.apply(this.element);
	}
	
	@Override
	public MutableList<E> mutable()
	{
		return MutableList.apply(this.element);
	}
	
	@Override
	public String toString()
	{
		return new StringBuilder().append('[').append(this.element).append(']').toString();
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
}
