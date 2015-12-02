package dyvil.collection.immutable;

import dyvil.collection.*;
import dyvil.collection.iterator.AppendIterator;
import dyvil.collection.iterator.PrependIterator;
import dyvil.collection.mutable.LinkedList;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class PrependList<E> implements ImmutableList<E>
{
	private static final long serialVersionUID = -989114482136946209L;
	
	private transient E                head;
	private transient ImmutableList<E> tail;
	
	private transient int size;
	
	public PrependList(E element)
	{
		this.head = element;
		this.tail = EmptyList.instance;
		this.size = 1;
	}
	
	public PrependList(E head, ImmutableList<E> tail)
	{
		this.head = head;
		this.tail = tail;
		this.size = 1 + tail.size();
	}
	
	@Override
	public int size()
	{
		return this.size;
	}
	
	@Override
	public boolean isEmpty()
	{
		return false;
	}
	
	@Override
	public Iterator<E> iterator()
	{
		return new PrependIterator<E>(this.head, this.tail.iterator());
	}
	
	@Override
	public Iterator<E> reverseIterator()
	{
		return new AppendIterator<E>(this.tail.reverseIterator(), this.head);
	}
	
	@Override
	public E subscript(int index)
	{
		if (index == 0)
		{
			return this.head;
		}
		return this.tail.subscript(index - 1);
	}
	
	@Override
	public E get(int index)
	{
		if (index == 0)
		{
			return this.head;
		}
		return this.tail.get(index - 1);
	}
	
	@Override
	public ImmutableList<E> subList(int startIndex, int length)
	{
		if (startIndex == 0)
		{
			return new PrependList<E>(this.head, this.tail.subList(startIndex - 1, length - 1));
		}
		return this.tail.subList(startIndex - 1, length - 1);
	}
	
	@Override
	public ImmutableList<E> $plus(E element)
	{
		return new PrependList<E>(this.head, this.tail.$plus(element));
	}
	
	@Override
	public ImmutableList<? extends E> $plus$plus(Collection<? extends E> collection)
	{
		return new PrependList<E>(this.head, (ImmutableList<E>) this.tail.$plus$plus(collection));
	}
	
	@Override
	public ImmutableList<E> $minus(Object element)
	{
		if (Objects.equals(element, this.head))
		{
			return this.tail.$minus(element);
		}
		return new PrependList<E>(this.head, this.tail.$minus(element));
	}
	
	@Override
	public ImmutableList<? extends E> $minus$minus(Collection<?> collection)
	{
		if (collection.contains(this.head))
		{
			return this.tail.$minus$minus(collection);
		}
		return new PrependList<E>(this.head, (ImmutableList<E>) this.tail.$minus$minus(collection));
	}
	
	@Override
	public ImmutableList<? extends E> $amp(Collection<? extends E> collection)
	{
		if (!collection.contains(this.head))
		{
			return this.tail.$amp(collection);
		}
		return new PrependList<E>(this.head, (ImmutableList<E>) this.tail.$amp(collection));
	}
	
	@Override
	public <R> ImmutableList<R> mapped(Function<? super E, ? extends R> mapper)
	{
		return new PrependList<R>(mapper.apply(this.head), this.tail.mapped(mapper));
	}
	
	@Override
	public <R> ImmutableList<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		ImmutableList<R> head = EmptyList.instance;
		for (R element : mapper.apply(this.head))
		{
			head = new AppendList<R>(head, element);
		}
		for (E element : this.tail)
		{
			for (R result : mapper.apply(element))
			{
				head = new AppendList<R>(head, result);
			}
		}
		return head;
	}
	
	@Override
	public ImmutableList<E> filtered(Predicate<? super E> condition)
	{
		if (!condition.test(this.head))
		{
			return this.tail.filtered(condition);
		}
		return new PrependList<E>(this.head, this.tail.filtered(condition));
	}
	
	@Override
	public ImmutableList<E> reversed()
	{
		return new AppendList(this.tail.reversed(), this.head);
	}
	
	private static <E> ImmutableList<E> fromArray(Object[] array, int length)
	{
		ImmutableList<E> list = EmptyList.instance;
		for (int i = length - 1; i >= 0; i--)
		{
			list = new PrependList<E>((E) array[i], list);
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
		int size = Set.sortDistinct((E[]) array, this.size, comparator);
		return fromArray(array, size);
	}
	
	@Override
	public int indexOf(Object element)
	{
		if (Objects.equals(element, this.head))
		{
			return 0;
		}
		int i = this.tail.indexOf(element);
		return i >= 0 ? i + 1 : -1;
	}
	
	@Override
	public int lastIndexOf(Object element)
	{
		int i = this.tail.lastIndexOf(element);
		if (i >= 0)
		{
			return i + 1;
		}
		return Objects.equals(element, this.head) ? 0 : -1;
	}
	
	@Override
	public void toArray(int index, Object[] store)
	{
		store[index] = this.head;
		this.tail.toArray(index + 1, store);
	}
	
	@Override
	public ImmutableList<E> copy()
	{
		return new PrependList<E>(this.head, this.tail.copy());
	}
	
	@Override
	public MutableList<E> mutable()
	{
		LinkedList<E> list = new LinkedList<E>();
		list.addFirst(this.head);
		list.addAll(this.tail);
		return list;
	}
	
	@Override
	public java.util.List<E> toJava()
	{
		java.util.LinkedList<E> list = new java.util.LinkedList<>();
		list.addFirst(this.head);
		for (E element : this.tail)
		{
			list.addLast(element);
		}
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
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
		
		out.writeObject(this.head);
		out.writeObject(this.tail);
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		
		this.head = (E) in.readObject();
		this.tail = (ImmutableList<E>) in.readObject();
		this.size = 1 + this.tail.size();
	}
}
