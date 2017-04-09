package dyvil.collection.mutable;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.Collection;
import dyvil.collection.Deque;
import dyvil.collection.*;
import dyvil.collection.List;
import dyvil.collection.Set;
import dyvil.collection.immutable.AppendList;
import dyvil.lang.LiteralConvertible;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

@LiteralConvertible.FromArray
public class LinkedList<E> implements MutableList<E>, Deque<E>
{
	private static final long serialVersionUID = 7185956993705123890L;

	protected static class Node<E>
	{
		E item;
		@Nullable Node<E> next;
		@Nullable Node<E> prev;

		Node(@Nullable Node<E> prev, E element, @Nullable Node<E> next)
		{
			this.item = element;
			this.next = next;
			this.prev = prev;
		}
	}

	protected transient int     size;
	@Nullable
	protected transient Node<E> first;
	@Nullable
	protected transient Node<E> last;

	@NonNull
	public static <E> LinkedList<E> apply()
	{
		return new LinkedList<>();
	}

	@NonNull
	@SafeVarargs
	public static <E> LinkedList<E> apply(@NonNull E... elements)
	{
		LinkedList<E> list = new LinkedList<>();
		for (E element : elements)
		{
			list.addLast(element);
		}
		return list;
	}

	public LinkedList()
	{
	}

	LinkedList(@Nullable Node<E> first, @Nullable Node<E> last, int size)
	{
		this.first = first;
		this.last = last;
		this.size = size;
	}

	@Override
	public int size()
	{
		return this.size;
	}

	@NonNull
	@Override
	public Iterator<E> iterator()
	{
		return new Iterator<E>()
		{
			@Nullable Node<E> lastReturned;
			@Nullable Node<E> next = LinkedList.this.first;

			@Override
			public boolean hasNext()
			{
				return this.next != null;
			}

			@Nullable
			@Override
			public E next()
			{
				if (this.next == null)
				{
					throw new NoSuchElementException();
				}

				this.lastReturned = this.next;
				this.next = this.next.next;
				return this.lastReturned.item;
			}

			@Override
			public void remove()
			{
				if (this.lastReturned == null)
				{
					throw new IllegalStateException();
				}

				Node<E> lastNext = this.lastReturned.next;
				LinkedList.this.unlink(this.lastReturned);
				if (this.next == this.lastReturned)
				{
					this.next = lastNext;
				}
				this.lastReturned = null;
			}

			@NonNull
			@Override
			public String toString()
			{
				return "LinkedListIterator(" + LinkedList.this + ")";
			}
		};
	}

	@NonNull
	@Override
	public Iterator<E> reverseIterator()
	{
		return new Iterator<E>()
		{
			@Nullable Node<E> lastReturned;
			@Nullable Node<E> prev = LinkedList.this.last;

			@Override
			public boolean hasNext()
			{
				return this.prev != null;
			}

			@Override
			public E next()
			{
				this.lastReturned = this.prev;
				this.prev = this.prev.prev;
				return this.lastReturned.item;
			}

			@Override
			public void remove()
			{
				if (this.lastReturned == null)
				{
					throw new IllegalStateException();
				}

				Node<E> lastPrev = this.lastReturned.prev;
				LinkedList.this.unlink(this.lastReturned);
				if (this.prev == this.lastReturned)
				{
					this.prev = lastPrev;
				}
				this.lastReturned = null;
			}

			@Override
			public @NonNull String toString()
			{
				return "LinkedListDescendingIterator(" + LinkedList.this + ")";
			}
		};
	}

	@Override
	public <R> R foldLeft(R initialValue, @NonNull BiFunction<? super R, ? super E, ? extends R> reducer)
	{
		for (Node<E> node = this.first; node != null; node = node.next)
		{
			initialValue = reducer.apply(initialValue, node.item);
		}
		return initialValue;
	}

	@Override
	public <R> R foldRight(R initialValue, @NonNull BiFunction<? super R, ? super E, ? extends R> reducer)
	{
		for (Node<E> node = this.last; node != null; node = node.prev)
		{
			initialValue = reducer.apply(initialValue, node.item);
		}
		return initialValue;
	}

	@Nullable
	@Override
	public E reduceLeft(@NonNull BiFunction<? super E, ? super E, ? extends E> reducer)
	{
		if (this.size == 0)
		{
			return null;
		}

		Node<E> node = this.first;
		@SuppressWarnings("ConstantConditions") E initialValue = node.item;
		do
		{
			if ((node = node.next) == null)
			{
				return initialValue;
			}
			initialValue = reducer.apply(initialValue, node.item);
		}
		while (true);
	}

	@Nullable
	@Override
	public E reduceRight(@NonNull BiFunction<? super E, ? super E, ? extends E> reducer)
	{
		if (this.size == 0)
		{
			return null;
		}

		Node<E> node = this.last;
		@SuppressWarnings("ConstantConditions") E initialValue = node.item;
		do
		{
			if ((node = node.prev) == null)
			{
				return initialValue;
			}
			initialValue = reducer.apply(initialValue, node.item);
		}
		while (true);
	}

	@Override
	public boolean contains(Object element)
	{
		for (Node<E> node = this.first; node != null; node = node.next)
		{
			if (Objects.equals(element, node.item))
			{
				return true;
			}
		}
		return false;
	}

	protected @NonNull Node<E> nodeAt(int index)
	{
		List.rangeCheck(index, this.size);

		Node<E> node = this.first;
		for (; index > 0; index--)
		{
			assert node != null;

			node = node.next;
		}

		//noinspection ConstantConditions
		return node;
	}

	@Override
	public E get(int index)
	{
		List.rangeCheck(index, this.size);
		return this.nodeAt(index).item;
	}

	@Nullable
	@Override
	public E getFirst()
	{
		return this.first == null ? null : this.first.item;
	}

	@Nullable
	@Override
	public E getLast()
	{
		return this.last == null ? null : this.last.item;
	}

	@NonNull
	@Override
	public MutableList<E> subList(int startIndex, int length)
	{
		List.rangeCheck(startIndex, this.size);
		List.rangeCheck(startIndex + length - 1, this.size);

		final LinkedList<E> copy = new LinkedList<>();

		Node<E> node = this.nodeAt(startIndex);
		for (; length > 0; length--)
		{
			assert node != null;

			copy.addLast(node.item);
			node = node.next;
		}

		return copy;
	}

	@NonNull
	@Override
	public List<E> reversed()
	{
		LinkedList<E> ll = new LinkedList<>();

		for (Node<E> node = this.first; node != null; node = node.next)
		{
			ll.addFirst(node.item);
		}
		return ll;
	}

	@NonNull
	@Override
	public MutableList<E> sorted()
	{
		LinkedList<E> copy = new LinkedList<>();
		Object[] array = this.toArray();
		Arrays.sort(array);
		copy.size = this.size;
		copy.fromArray(array, this.size);
		return copy;
	}

	@NonNull
	@Override
	public MutableList<E> sorted(@NonNull Comparator<? super E> comparator)
	{
		LinkedList<E> copy = new LinkedList<>();
		Object[] array = this.toArray();
		Arrays.sort((E[]) array, comparator);
		copy.fromArray(array, this.size);
		return copy;
	}

	@NonNull
	@Override
	public MutableList<E> distinct()
	{
		LinkedList<E> copy = new LinkedList<>();
		Object[] array = this.toArray();
		copy.fromArray(array, Set.distinct(array, this.size));
		return copy;
	}

	@NonNull
	@Override
	public MutableList<E> distinct(@NonNull Comparator<? super E> comparator)
	{
		LinkedList<E> copy = new LinkedList<>();
		Object[] array = this.toArray();
		copy.fromArray(array, Set.sortDistinct((E[]) array, this.size, comparator));
		return copy;
	}

	@Override
	public void addElement(E element)
	{
		this.addLast(element);
	}

	@Nullable
	@Override
	public E set(int index, E element)
	{
		List.rangeCheck(index, this.size);

		final Node<E> node = this.nodeAt(index);
		final E oldElement = node.item;
		node.item = element;
		return oldElement;
	}

	@Override
	public void addFirst(E element)
	{
		this.size++;
		final Node<E> node = new Node<>(null, element, this.first);
		if (this.first != null)
		{
			this.first.prev = node;
		}
		else
		{
			this.last = node;
		}
		this.first = node;
	}

	@Override
	public void addLast(E element)
	{
		this.size++;
		final Node<E> node = new Node<>(this.last, element, null);
		if (this.last != null)
		{
			this.last.next = node;
		}
		else
		{
			this.first = node;
		}
		this.last = node;
	}

	@Nullable
	@Override
	public E setResizing(int index, E element)
	{
		while (index >= this.size)
		{
			this.addLast(null);
		}
		List.rangeCheck(index, this.size);

		final Node<E> node = this.nodeAt(index);

		final E e = node.item;
		node.item = element;
		return e;
	}

	@Override
	public void insert(int index, E element)
	{
		if (index == 0)
		{
			this.addFirst(element);
			return;
		}
		if (index == this.size)
		{
			this.addLast(element);
			return;
		}

		List.rangeCheck(index, this.size);
		final Node<E> nodeAt = this.nodeAt(index);

		assert nodeAt.prev != null;

		final Node<E> newNode = new Node<>(nodeAt.prev, element, nodeAt);
		nodeAt.prev.next = newNode;
		nodeAt.prev = newNode;
	}

	@Override
	public void removeAt(int index)
	{
		List.rangeCheck(index, this.size);
		//noinspection ConstantConditions
		this.unlink(this.nodeAt(index));
	}

	protected void unlink(@NonNull Node<E> node)
	{
		final Node<E> next = node.next;
		final Node<E> prev = node.prev;

		if (prev == null)
		{
			this.first = next;
		}
		else
		{
			prev.next = next;
			node.prev = null;
		}

		if (next == null)
		{
			this.last = prev;
		}
		else
		{
			next.prev = prev;
			node.next = null;
		}

		node.item = null;
		this.size--;
	}

	@Nullable
	@Override
	public E removeFirst()
	{
		if (this.first == null)
		{
			return null;
		}

		E e = this.first.item;
		this.unlink(this.first);
		return e;
	}

	@Nullable
	@Override
	public E removeLast()
	{
		if (this.last == null)
		{
			return null;
		}

		E e = this.last.item;
		this.unlink(this.last);
		return e;
	}

	@Override
	public boolean remove(Object element)
	{
		boolean removed = false;
		Node<E> node = this.first;
		while (node != null)
		{
			if (Objects.equals(node.item, element))
			{
				Node<E> next = node.next;
				this.unlink(node);
				removed = true;
				node = next;
				continue;
			}
			node = node.next;
		}
		return removed;
	}

	@Override
	public boolean removeFirst(Object element)
	{
		Node<E> node = this.first;
		for (; node != null; node = node.next)
		{
			if (Objects.equals(node.item, element))
			{
				this.unlink(node);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean removeLast(Object element)
	{
		Node<E> node = this.last;
		for (; node != null; node = node.prev)
		{
			if (Objects.equals(node.item, element))
			{
				this.unlink(node);
				return true;
			}
		}
		return false;
	}

	@Nullable
	@Override
	public E peek(int index)
	{
		if (index >= this.size)
		{
			return null;
		}
		return this.get(index);
	}

	@Override
	public void clear()
	{
		Node<E> node = this.first;
		while (node != null)
		{
			Node<E> next = node.next;
			node.prev = node.next = null;
			node.item = null;
			node = next;
		}

		this.first = this.last = null;
		this.size = 0;
	}

	@Override
	public void map(@NonNull Function<? super E, ? extends E> mapper)
	{
		for (Node<E> node = this.first; node != null; node = node.next)
		{
			node.item = mapper.apply(node.item);
		}
	}

	@Override
	public void flatMap(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends E>> mapper)
	{
		int size = 0;
		Node<E> first = null;
		Node<E> last = null;

		for (Node<E> node = this.first; node != null; node = node.next)
		{
			for (E e : mapper.apply(node.item))
			{
				size++;
				final Node<E> current = new Node<>(first, e, null);
				if (last != null)
				{
					last.next = current;
				}
				else
				{
					first = current;
				}
				last = current;
			}
		}

		this.size = size;
		this.first = first;
		this.last = last;
	}

	@Override
	public void filter(@NonNull Predicate<? super E> condition)
	{
		Node<E> node = this.first;
		while (node != null)
		{
			Node<E> next = node.next;
			if (!condition.test(node.item))
			{
				this.unlink(node);
			}
			node = next;
		}
	}

	@Override
	public void reverse()
	{
		Node<E> temp = this.first;
		this.first = this.last;
		Node<E> p = this.last = temp;

		while (p != null)
		{
			temp = p.next;
			p.next = p.prev;
			p = p.prev = temp;
		}
	}

	@Override
	public void sort()
	{
		Object[] array = this.toArray();
		Arrays.sort(array);
		this.fromArray(array, this.size);
	}

	@Override
	public void sort(@NonNull Comparator<? super E> comparator)
	{
		Object[] array = this.toArray();
		Arrays.sort((E[]) array, comparator);
		this.fromArray(array, this.size);
	}

	@Override
	public void distinguish()
	{
		Object[] array = this.toArray();
		this.fromArray(array, Set.distinct(array, this.size));
	}

	@Override
	public void distinguish(@NonNull Comparator<? super E> comparator)
	{
		Object[] array = this.toArray();
		this.fromArray(array, Set.distinct(array, this.size));
	}

	protected void fromArray(Object[] array, int size)
	{
		this.clear();
		for (int i = 0; i < size; i++)
		{
			this.addLast((E) array[i]);
		}
		this.size = size;
	}

	@Override
	public int indexOf(Object element)
	{
		Node<E> node = this.first;
		for (int index = 0; node != null; index++, node = node.next)
		{
			if (Objects.equals(node.item, element))
			{
				return index;
			}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object element)
	{
		Node<E> node = this.last;
		for (int index = 0; node != null; index++, node = node.prev)
		{
			if (Objects.equals(node.item, element))
			{
				return index;
			}
		}
		return -1;
	}

	@Override
	public void toArray(int index, Object @NonNull [] store)
	{
		for (Node<E> node = this.first; node != null; node = node.next)
		{
			store[index++] = node.item;
		}
	}

	@NonNull
	@Override
	public <R> MutableList<R> emptyCopy()
	{
		return new LinkedList<>();
	}

	@NonNull
	@Override
	public <R> MutableList<R> emptyCopy(int newCapacity)
	{
		return this.emptyCopy();
	}

	@NonNull
	@Override
	public ImmutableList<E> immutable()
	{
		return AppendList.from(this);
	}

	@Override
	public <RE> ImmutableList.Builder<RE> immutableBuilder()
	{
		return AppendList.builder();
	}

	@Override
	public <RE> ImmutableList.Builder<RE> immutableBuilder(int capacity)
	{
		return AppendList.builder();
	}

	@NonNull
	@Override
	public LinkedList<E> copy()
	{
		LinkedList<E> copy = new LinkedList<>();
		for (Node<E> node = this.first; node != null; node = node.next)
		{
			copy.addLast(node.item);
		}
		return copy;
	}

	@Override
	public java.util.@NonNull List<E> toJava()
	{
		java.util.LinkedList<E> list = new java.util.LinkedList<>();
		for (Node<E> node = this.first; node != null; node = node.next)
		{
			list.addLast(node.item);
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

		out.writeInt(this.size);

		for (Node<E> node = this.first; node != null; node = node.next)
		{
			out.writeObject(node.item);
		}
	}

	private void readObject(java.io.@NonNull ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();

		this.size = in.readInt();

		if (this.size <= 0)
		{
			return;
		}

		Node<E> node = this.first = new Node<>(null, (E) in.readObject(), null);
		for (int i = 1; i < this.size; i++)
		{
			Node<E> next = new Node<>(node, (E) in.readObject(), null);
			node.next = next;
			node = next;
		}

		this.last = node;
	}
}
