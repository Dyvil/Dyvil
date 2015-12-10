package dyvil.collection.iterator;

import java.util.Iterator;

public class PrependIterator<E> implements Iterator<E>
{
	private final E                     head;
	private final Iterator<? extends E> tail;
	
	private boolean returned;
	
	public PrependIterator(E head, Iterator<? extends E> tail)
	{
		this.head = head;
		this.tail = tail;
	}
	
	@Override
	public boolean hasNext()
	{
		return !this.returned || this.tail.hasNext();
	}
	
	@Override
	public E next()
	{
		if (!this.returned)
		{
			this.returned = true;
			return this.head;
		}
		return this.tail.next();
	}
}
