package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.NonNull;
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

@Immutable
public class PrependList<E> implements ImmutableList<E>
{
	private static final long serialVersionUID = -989114482136946209L;

	private transient E                head;
	private transient ImmutableList<E> tail;

	private transient int size;

	@NonNull
	public static <E> PrependList<E> apply(E head)
	{
		return new PrependList<>(head);
	}

	public PrependList(E element)
	{
		this.head = element;
		this.tail = (ImmutableList<E>) EmptyList.instance;
		this.size = 1;
	}

	public PrependList(E head, @NonNull ImmutableList<E> tail)
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

	@NonNull
	@Override
	public Iterator<E> iterator()
	{
		return new PrependIterator<>(this.head, this.tail.iterator());
	}

	@NonNull
	@Override
	public Iterator<E> reverseIterator()
	{
		return new AppendIterator<>(this.tail.reverseIterator(), this.head);
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

	@NonNull
	@Override
	public ImmutableList<E> subList(int startIndex, int length)
	{
		if (startIndex == 0)
		{
			return new PrependList<>(this.head, this.tail.subList(startIndex - 1, length - 1));
		}
		return this.tail.subList(startIndex - 1, length - 1);
	}

	@NonNull
	@Override
	public ImmutableList<E> added(E element)
	{
		return new PrependList<>(this.head, this.tail.added(element));
	}

	@NonNull
	@Override
	public ImmutableList<E> union(@NonNull Collection<? extends E> collection)
	{
		return new PrependList<>(this.head, (ImmutableList<E>) this.tail.union(collection));
	}

	@NonNull
	@Override
	public ImmutableList<E> removed(Object element)
	{
		if (Objects.equals(element, this.head))
		{
			return this.tail.removed(element);
		}
		return new PrependList<>(this.head, this.tail.removed(element));
	}

	@NonNull
	@Override
	public ImmutableList<E> difference(@NonNull Collection<?> collection)
	{
		if (collection.contains(this.head))
		{
			return this.tail.difference(collection);
		}
		return new PrependList<>(this.head, (ImmutableList<E>) this.tail.difference(collection));
	}

	@NonNull
	@Override
	public ImmutableList<E> intersection(@NonNull Collection<? extends E> collection)
	{
		if (!collection.contains(this.head))
		{
			return this.tail.intersection(collection);
		}
		return new PrependList<>(this.head, (ImmutableList<E>) this.tail.intersection(collection));
	}

	@NonNull
	@Override
	public <R> ImmutableList<R> mapped(@NonNull Function<? super E, ? extends R> mapper)
	{
		return new PrependList<>(mapper.apply(this.head), this.tail.mapped(mapper));
	}

	@NonNull
	@Override
	public <R> ImmutableList<R> flatMapped(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends R>> mapper)
	{
		ImmutableList<R> head = (ImmutableList<R>) EmptyList.instance;
		for (R element : mapper.apply(this.head))
		{
			head = new AppendList<>(head, element);
		}
		for (E element : this.tail)
		{
			for (R result : mapper.apply(element))
			{
				head = new AppendList<>(head, result);
			}
		}
		return head;
	}

	@NonNull
	@Override
	public ImmutableList<E> filtered(@NonNull Predicate<? super E> predicate)
	{
		if (!predicate.test(this.head))
		{
			return this.tail.filtered(predicate);
		}
		return new PrependList<>(this.head, this.tail.filtered(predicate));
	}

	@NonNull
	@Override
	public ImmutableList<E> reversed()
	{
		return new AppendList<>(this.tail.reversed(), this.head);
	}

	@NonNull
	private static <E> ImmutableList<E> fromArray(Object[] array, int length)
	{
		ImmutableList<E> list = (ImmutableList<E>) EmptyList.instance;
		for (int i = length - 1; i >= 0; i--)
		{
			list = new PrependList<>((E) array[i], list);
		}
		return list;
	}

	@NonNull
	@Override
	public ImmutableList<E> sorted()
	{
		Object[] array = this.toArray();
		Arrays.sort(array);
		return fromArray(array, this.size);
	}

	@NonNull
	@Override
	public ImmutableList<E> sorted(@NonNull Comparator<? super E> comparator)
	{
		Object[] array = this.toArray();
		Arrays.sort((E[]) array, comparator);
		return fromArray(array, this.size);
	}

	@NonNull
	@Override
	public ImmutableList<E> distinct()
	{
		Object[] array = this.toArray();
		int size = Set.distinct(array, this.size);
		return fromArray(array, size);
	}

	@NonNull
	@Override
	public ImmutableList<E> distinct(@NonNull Comparator<? super E> comparator)
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
	public void toArray(int index, Object @NonNull [] store)
	{
		store[index] = this.head;
		this.tail.toArray(index + 1, store);
	}

	@NonNull
	@Override
	public ImmutableList<E> copy()
	{
		return new PrependList<>(this.head, this.tail.copy());
	}

	@NonNull
	@Override
	public <RE> MutableList<RE> emptyCopy()
	{
		return MutableList.apply();
	}

	@NonNull
	@Override
	public <RE> MutableList<RE> emptyCopy(int capacity)
	{
		return MutableList.withCapacity(capacity);
	}

	@NonNull
	@Override
	public MutableList<E> mutable()
	{
		LinkedList<E> list = new LinkedList<>();
		list.addFirst(this.head);
		list.addAll(this.tail);
		return list;
	}

	@Override
	public <RE> Builder<RE> immutableBuilder()
	{
		return ImmutableList.builder();
	}

	@Override
	public <RE> Builder<RE> immutableBuilder(int capacity)
	{
		return ImmutableList.builder(capacity);
	}

	@Override
	public java.util.@NonNull List<E> toJava()
	{
		java.util.LinkedList<E> list = new java.util.LinkedList<>();
		list.addFirst(this.head);
		for (E element : this.tail)
		{
			list.addLast(element);
		}
		return list;
	}

	@NonNull
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

	private void writeObject(java.io.@NonNull ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();

		out.writeObject(this.head);
		out.writeObject(this.tail);
	}

	private void readObject(java.io.@NonNull ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();

		this.head = (E) in.readObject();
		this.tail = (ImmutableList<E>) in.readObject();
		this.size = 1 + this.tail.size();
	}
}
