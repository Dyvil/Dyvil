package dyvil.collection;

import java.util.Iterator;

public interface Deque<E> extends Queue<E>, Stack<E>
{
	// Collection Methods
	
	@Override
	public default boolean contains(Object o)
	{
		return Collection.iterableContains(this, o);
	}
	
	@Override
	public int size();
	
	@Override
	public Iterator<E> iterator();
	
	public Iterator<E> reverseIterator();
	
	// Deque Methods
	
	@Override
	public void clear();
	
	public void addFirst(E e);
	
	public void addLast(E e);
	
	public E removeFirst();
	
	public E removeLast();
	
	public E getFirst();
	
	public E getLast();
	
	public boolean removeFirst(Object o);
	
	public boolean removeLast(Object o);
	
	// Queue Methods
	
	@Override
	public default void offer(E e)
	{
		this.addLast(e);
	}
	
	@Override
	public default E remove()
	{
		return this.removeLast();
	}
	
	@Override
	public default E element()
	{
		return this.getLast();
	}
	
	// Stack Methods
	
	@Override
	public default void push(E e)
	{
		this.addFirst(e);
	}
	
	@Override
	public default E pop()
	{
		return this.removeFirst();
	}
	
	@Override
	public default E peek()
	{
		return this.getFirst();
	}
	
	@Override
	public Deque<E> copy();
}
