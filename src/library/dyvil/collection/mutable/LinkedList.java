package dyvil.collection.mutable;

import dyvil.collection.Collection;
import dyvil.collection.Deque;
import dyvil.collection.*;
import dyvil.collection.List;
import dyvil.collection.Set;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

@NilConvertible
@ArrayConvertible
public class LinkedList<E> implements MutableList<E>, Deque<E>
{
	private static final long serialVersionUID = 7185956993705123890L;
	
	protected static class Node<E>
	{
		E       item;
		Node<E> next;
		Node<E> prev;
		
		Node(Node<E> prev, E element, Node<E> next)
		{
			this.item = element;
			this.next = next;
			this.prev = prev;
		}
	}
	
	protected transient int     size;
	protected transient Node<E> first;
	protected transient Node<E> last;
	
	public static <E> LinkedList<E> apply()
	{
		return new LinkedList<>();
	}
	
	@SafeVarargs
	public static <E> LinkedList<E> apply(E... elements)
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
	
	LinkedList(Node<E> first, Node<E> last, int size)
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
	
	@Override
	public Iterator<E> iterator()
	{
		return new Iterator<E>()
		{
			Node<E> lastReturned;
			Node<E> next = LinkedList.this.first;
			
			@Override
			public boolean hasNext()
			{
				return this.next != null;
			}
			
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
			
			@Override
			public String toString()
			{
				return "LinkedListIterator(" + LinkedList.this + ")";
			}
		};
	}
	
	@Override
	public Iterator<E> reverseIterator()
	{
		return new Iterator<E>()
		{
			Node<E> lastReturned;
			Node<E> prev = LinkedList.this.last;
			
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
			public String toString()
			{
				return "LinkedListDescendingIterator(" + LinkedList.this + ")";
			}
		};
	}
	
	@Override
	public <R> R foldLeft(R initialValue, BiFunction<? super R, ? super E, ? extends R> reducer)
	{
		for (Node<E> node = this.first; node != null; node = node.next)
		{
			initialValue = reducer.apply(initialValue, node.item);
		}
		return initialValue;
	}
	
	@Override
	public <R> R foldRight(R initialValue, BiFunction<? super R, ? super E, ? extends R> reducer)
	{
		for (Node<E> node = this.last; node != null; node = node.prev)
		{
			initialValue = reducer.apply(initialValue, node.item);
		}
		return initialValue;
	}
	
	@Override
	public E reduceLeft(BiFunction<? super E, ? super E, ? extends E> reducer)
	{
		if (this.size == 0)
		{
			return null;
		}
		
		Node<E> node = this.first;
		E initialValue = node.item;
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
	
	@Override
	public E reduceRight(BiFunction<? super E, ? super E, ? extends E> reducer)
	{
		if (this.size == 0)
		{
			return null;
		}
		
		Node<E> node = this.last;
		E initialValue = node.item;
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
	
	protected Node<E> nodeAt(int index)
	{
		Node<E> node = this.first;
		for (; index > 0; index--)
		{
			node = node.next;
		}
		return node;
	}
	
	@Override
	public E get(int index)
	{
		List.rangeCheck(index, this.size);
		return this.nodeAt(index).item;
	}
	
	@Override
	public E getFirst()
	{
		return this.first == null ? null : this.first.item;
	}
	
	@Override
	public E getLast()
	{
		return this.last == null ? null : this.last.item;
	}
	
	@Override
	public MutableList<E> subList(int startIndex, int length)
	{
		List.rangeCheck(startIndex, this.size);
		List.rangeCheck(startIndex + length - 1, this.size);
		
		LinkedList<E> copy = new LinkedList<>();
		Node<E> node = this.nodeAt(startIndex);
		for (; length > 0; length--)
		{
			copy.addLast(node.item);
			node = node.next;
		}
		
		return copy;
	}
	
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
	
	@Override
	public MutableList<E> sorted(Comparator<? super E> comparator)
	{
		LinkedList<E> copy = new LinkedList<>();
		Object[] array = this.toArray();
		Arrays.sort((E[]) array, comparator);
		copy.fromArray(array, this.size);
		return copy;
	}
	
	@Override
	public MutableList<E> distinct()
	{
		LinkedList<E> copy = new LinkedList<>();
		Object[] array = this.toArray();
		copy.fromArray(array, Set.distinct(array, this.size));
		return copy;
	}
	
	@Override
	public MutableList<E> distinct(Comparator<? super E> comparator)
	{
		LinkedList<E> copy = new LinkedList<>();
		Object[] array = this.toArray();
		copy.fromArray(array, Set.sortDistinct((E[]) array, this.size, comparator));
		return copy;
	}
	
	@Override
	public void $plus$eq(E element)
	{
		this.addLast(element);
	}

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
	public void addFirst(E e)
	{
		this.size++;
		Node<E> n = new Node<>(null, e, this.first);
		if (this.first != null)
		{
			this.first.prev = n;
		}
		else
		{
			this.last = n;
		}
		this.first = n;
	}
	
	@Override
	public void addLast(E element)
	{
		this.size++;
		Node<E> n = new Node<>(this.last, element, null);
		if (this.last != null)
		{
			this.last.next = n;
		}
		else
		{
			this.first = n;
		}
		this.last = n;
	}
	
	@Override
	public E setResizing(int index, E element)
	{
		while (index >= this.size)
		{
			this.addLast(null);
		}

		Node<E> node = this.nodeAt(index);
		E e = node.item;
		node.item = element;
		return e;
	}

	@Override
	public void insert(int index, E element)
	{
		if (index == this.size)
		{
			this.addLast(element);
			return;
		}

		List.rangeCheck(index, this.size);
		Node<E> node = this.nodeAt(index);
		Node<E> n = new Node<>(node.prev, element, node);
		node.prev.next = n;
		node.prev = n;
	}

	@Override
	public void removeAt(int index)
	{
		List.rangeCheck(index, this.size);
		this.unlink(this.nodeAt(index));
	}
	
	protected void unlink(Node<E> node)
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
	
	@Override
	public E removeFirst()
	{
		E e = this.first.item;
		this.unlink(this.first);
		return e;
	}
	
	@Override
	public E removeLast()
	{
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
	public boolean removeFirst(Object o)
	{
		Node<E> node = this.first;
		for (; node != null; node = node.next)
		{
			if (Objects.equals(node.item, o))
			{
				this.unlink(node);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean removeLast(Object o)
	{
		Node<E> node = this.last;
		for (; node != null; node = node.prev)
		{
			if (Objects.equals(node.item, o))
			{
				this.unlink(node);
				return true;
			}
		}
		return false;
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
	public void map(Function<? super E, ? extends E> mapper)
	{
		for (Node<E> node = this.first; node != null; node = node.next)
		{
			node.item = mapper.apply(node.item);
		}
	}
	
	@Override
	public void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper)
	{
		int size = 0;
		Node<E> first = null;
		Node<E> last = null;
		
		for (Node<E> node = this.first; node != null; node = node.next)
		{
			for (E e : mapper.apply(node.item))
			{
				size++;
				Node<E> n = new Node<>(first, e, null);
				if (last != null)
				{
					last.next = n;
				}
				else
				{
					first = n;
				}
				last = n;
			}
		}
		
		this.size = size;
		this.first = first;
		this.last = last;
	}
	
	@Override
	public void filter(Predicate<? super E> condition)
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
	public void sort(Comparator<? super E> comparator)
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
	public void distinguish(Comparator<? super E> comparator)
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
	public void toArray(int index, Object[] store)
	{
		for (Node<E> node = this.first; node != null; node = node.next)
		{
			store[index++] = node.item;
		}
	}
	
	@Override
	public <R> MutableList<R> emptyCopy()
	{
		return new LinkedList<>();
	}

	@Override
	public <R> MutableList<R> emptyCopy(int newCapacity)
	{
		return this.emptyCopy();
	}

	@Override
	public ImmutableList<E> immutable()
	{
		return ImmutableList.linked(this);
	}

	@Override
	public <RE> ImmutableList.Builder<RE> immutableBuilder()
	{
		return dyvil.collection.ImmutableList.builder(); // TODO linked builder
	}

	@Override
	public <RE> ImmutableList.Builder<RE> immutableBuilder(int capacity)
	{
		return dyvil.collection.ImmutableList.builder(capacity);
	}

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
	public java.util.List<E> toJava()
	{
		java.util.LinkedList<E> list = new java.util.LinkedList<>();
		for (Node<E> node = this.first; node != null; node = node.next)
		{
			list.addLast(node.item);
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
		
		out.writeInt(this.size);
		
		for (Node<E> node = this.first; node != null; node = node.next)
		{
			out.writeObject(node.item);
		}
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
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
