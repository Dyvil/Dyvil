package dyvil.collection.immutable;

import dyvil.annotation._internal.object;
import dyvil.array.ObjectArray;
import dyvil.collection.Collection;
import dyvil.collection.ImmutableList;
import dyvil.collection.List;
import dyvil.collection.MutableList;
import dyvil.collection.iterator.EmptyIterator;
import dyvil.lang.literal.NilConvertible;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@NilConvertible
public
@object
class EmptyList<E> implements ImmutableList<E>
{
	private static final long serialVersionUID = -6059901529322971155L;
	
	public static final EmptyList instance = new EmptyList();
	
	public static <E> EmptyList<E> apply()
	{
		return instance;
	}
	
	private EmptyList()
	{
	}
	
	@Override
	public int size()
	{
		return 0;
	}
	
	@Override
	public boolean isEmpty()
	{
		return true;
	}
	
	@Override
	public Iterator<E> iterator()
	{
		return EmptyIterator.instance;
	}
	
	@Override
	public Iterator<E> reverseIterator()
	{
		return EmptyIterator.instance;
	}
	
	@Override
	public Spliterator<E> spliterator()
	{
		return Spliterators.emptySpliterator();
	}
	
	@Override
	public void forEach(Consumer<? super E> action)
	{
	}
	
	@Override
	public <R> R foldLeft(R initialValue, BiFunction<? super R, ? super E, ? extends R> reducer)
	{
		return initialValue;
	}
	
	@Override
	public <R> R foldRight(R initialValue, BiFunction<? super R, ? super E, ? extends R> reducer)
	{
		return initialValue;
	}
	
	@Override
	public E reduceLeft(BiFunction<? super E, ? super E, ? extends E> reducer)
	{
		return null;
	}
	
	@Override
	public E reduceRight(BiFunction<? super E, ? super E, ? extends E> reducer)
	{
		return null;
	}
	
	@Override
	public boolean contains(Object element)
	{
		return false;
	}
	
	@Override
	public E subscript(int index)
	{
		throw new IndexOutOfBoundsException("Empty List.apply()");
	}
	
	@Override
	public E get(int index)
	{
		return null;
	}
	
	@Override
	public ImmutableList<E> subList(int startIndex, int length)
	{
		if (startIndex > 0 || length > 0)
		{
			throw new IndexOutOfBoundsException("Empty List Slice out of range");
		}
		return this;
	}
	
	@Override
	public int indexOf(Object element)
	{
		return -1;
	}
	
	@Override
	public int lastIndexOf(Object element)
	{
		return -1;
	}
	
	@Override
	public ImmutableList<E> $plus(E element)
	{
		return new SingletonList(element);
	}
	
	@Override
	public ImmutableList<? extends E> $plus$plus(Collection<? extends E> collection)
	{
		return new ArrayList(collection);
	}
	
	@Override
	public ImmutableList<E> $minus(Object element)
	{
		return this;
	}
	
	@Override
	public ImmutableList<? extends E> $minus$minus(Collection<?> collection)
	{
		return this;
	}
	
	@Override
	public ImmutableList<? extends E> $amp(Collection<? extends E> collection)
	{
		return this;
	}
	
	@Override
	public <R> ImmutableList<R> mapped(Function<? super E, ? extends R> mapper)
	{
		return (ImmutableList<R>) this;
	}
	
	@Override
	public <R> ImmutableList<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		return (ImmutableList<R>) this;
	}
	
	@Override
	public ImmutableList<E> filtered(Predicate<? super E> condition)
	{
		return this;
	}
	
	@Override
	public ImmutableList<E> reversed()
	{
		return this;
	}
	
	@Override
	public ImmutableList<E> sorted()
	{
		return this;
	}
	
	@Override
	public ImmutableList<E> sorted(Comparator<? super E> comparator)
	{
		return this;
	}
	
	@Override
	public ImmutableList<E> distinct()
	{
		return this;
	}
	
	@Override
	public ImmutableList<E> distinct(Comparator<? super E> comparator)
	{
		return this;
	}
	
	@Override
	public Object[] toArray()
	{
		return ObjectArray.EMPTY;
	}
	
	@Override
	public E[] toArray(Class<E> type)
	{
		return (E[]) ObjectArray.EMPTY;
	}
	
	@Override
	public void toArray(int index, Object[] store)
	{
	}
	
	@Override
	public ImmutableList<E> copy()
	{
		return this;
	}
	
	@Override
	public MutableList<E> mutable()
	{
		return MutableList.apply();
	}
	
	@Override
	public java.util.List<E> toJava()
	{
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public String toString()
	{
		return "[]";
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
	
	private Object writeReplace() throws java.io.ObjectStreamException
	{
		return instance;
	}
	
	private Object readResolve() throws java.io.ObjectStreamException
	{
		return instance;
	}
}
