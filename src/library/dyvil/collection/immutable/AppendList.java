package dyvil.collection.immutable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.collection.*;
import dyvil.collection.iterator.AppendIterator;
import dyvil.collection.iterator.PrependIterator;
import dyvil.collection.mutable.LinkedList;

public class AppendList<E> implements ImmutableList<E>
{
	private final ImmutableList<E>	head;
	private final E					tail;
	
	private final int size;
	
	public AppendList(E element)
	{
		this.head = EmptyList.instance;
		this.tail = element;
		this.size = 1;
	}
	
	public AppendList(ImmutableList<E> head, E tail)
	{
		this.head = head;
		this.tail = tail;
		this.size = 1 + head.size();
	}
	
	@Override
	public int size()
	{
		return this.size;
	}
	
	@Override
	public Iterator<E> iterator()
	{
		return new AppendIterator<E>(this.head.iterator(), this.tail);
	}
	
	@Override
	public Iterator<E> reverseIterator()
	{
		return new PrependIterator<E>(this.tail, this.head.reverseIterator());
	}
	
	@Override
	public E subscript(int index)
	{
		if (index < this.size - 1)
		{
			return this.head.subscript(index);
		}
		return index == this.size - 1 ? this.tail : null;
	}
	
	@Override
	public E get(int index)
	{
		if (index < this.size - 1)
		{
			return this.head.subscript(index);
		}
		if (index == this.size - 1)
		{
			return this.tail;
		}
		throw new IndexOutOfBoundsException(index + " >= " + this.size);
	}
	
	@Override
	public ImmutableList<E> subList(int startIndex, int length)
	{
		if (startIndex + length == this.size - 1)
		{
			return new AppendList<E>(this.head.subList(startIndex, length - 1), this.tail);
		}
		return this.head.subList(startIndex, length - 1);
	}
	
	@Override
	public ImmutableList<E> $plus(E element)
	{
		return new AppendList<E>(this, element);
	}
	
	@Override
	public ImmutableList<? extends E> $plus$plus(Collection<? extends E> collection)
	{
		AppendList<E> ll = this;
		for (E element : collection)
		{
			ll = new AppendList<E>(ll, element);
		}
		return ll;
	}
	
	@Override
	public ImmutableList<E> $minus(Object element)
	{
		if (Objects.equals(element, this.tail))
		{
			return this.head.$minus(element);
		}
		return new AppendList<E>(this.head.$minus(element), this.tail);
	}
	
	@Override
	public ImmutableList<? extends E> $minus$minus(Collection<?> collection)
	{
		if (collection.contains(this.tail))
		{
			return this.head.$minus$minus(collection);
		}
		return new AppendList<E>((ImmutableList<E>) this.head.$minus$minus(collection), this.tail);
	}
	
	@Override
	public ImmutableList<? extends E> $amp(Collection<? extends E> collection)
	{
		if (!collection.contains(this.tail))
		{
			return this.head.$amp(collection);
		}
		return new AppendList<E>((ImmutableList<E>) this.head.$amp(collection), this.tail);
	}
	
	@Override
	public <R> ImmutableList<R> mapped(Function<? super E, ? extends R> mapper)
	{
		return new AppendList<R>(this.head.mapped(mapper), mapper.apply(this.tail));
	}
	
	@Override
	public <R> ImmutableList<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		ImmutableList<R> head = this.head.flatMapped(mapper);
		for (R element : mapper.apply(this.tail))
		{
			head = new AppendList<R>(head, element);
		}
		return head;
	}
	
	@Override
	public ImmutableList<E> filtered(Predicate<? super E> condition)
	{
		if (!condition.test(this.tail))
		{
			return this.head.filtered(condition);
		}
		return new AppendList<E>(this.head.filtered(condition), this.tail);
	}
	
	@Override
	public ImmutableList<E> reversed()
	{
		return new PrependList(this.tail, this.head.reversed());
	}
	
	private static <E> ImmutableList<E> fromArray(Object[] array, int length)
	{
		ImmutableList<E> list = EmptyList.instance;
		for (int i = 0; i < length; i++)
		{
			list = new AppendList<E>(list, (E) array[i]);
		}
		return list;
	}
	
	@Override
	public ImmutableList<E> sorted()
	{
		Object[] array = this.toArray();
		Arrays.sort(array);
		return fromArray(array, this.size);
	}
	
	@Override
	public ImmutableList<E> sorted(Comparator<? super E> comparator)
	{
		Object[] array = this.toArray();
		Arrays.sort((E[]) array, comparator);
		return fromArray(array, this.size);
	}
	
	@Override
	public ImmutableList<E> distinct()
	{
		Object[] array = this.toArray();
		int size = Set.distinct(array, this.size);
		return fromArray(array, size);
	}
	
	@Override
	public ImmutableList<E> distinct(Comparator<? super E> comparator)
	{
		Object[] array = this.toArray();
		int size = Set.distinct((E[]) array, this.size, comparator);
		return fromArray(array, size);
	}
	
	@Override
	public int indexOf(Object element)
	{
		int i = this.head.indexOf(element);
		if (i < 0)
		{
			return Objects.equals(element, this.tail) ? this.size - 1 : -1;
		}
		return i;
	}
	
	@Override
	public int lastIndexOf(Object element)
	{
		if (Objects.equals(element, this.tail))
		{
			return this.size - 1;
		}
		return this.head.lastIndexOf(element);
	}
	
	@Override
	public void toArray(int index, Object[] store)
	{
		this.head.toArray(index, store);
		store[index + this.size - 1] = this.tail;
	}
	
	@Override
	public ImmutableList<E> copy()
	{
		return new AppendList<E>(this.head.copy(), this.tail);
	}
	
	@Override
	public MutableList<E> mutable()
	{
		LinkedList<E> list = new LinkedList<E>();
		list.addAll(this.head);
		list.addLast(this.tail);
		return list;
	}
	
	@Override
	public java.util.List<E> toJava()
	{
		java.util.LinkedList<E> list = new java.util.LinkedList<>();
		for (E element : this.head)
		{
			list.addLast(element);
		}
		list.addLast(this.tail);
		return list;
	}
	
	@Override
	public String toString()
	{
		return Collection.collectionToString(this);
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
