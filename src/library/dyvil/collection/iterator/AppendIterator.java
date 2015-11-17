package dyvil.collection.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class AppendIterator<E> implements Iterator<E>
{
	private final Iterator<? extends E>	head;
	private final E						tail;
	
	private boolean returned;
	
	public AppendIterator(Iterator<? extends E> head, E tail)
	{
		this.head = head;
		this.tail = tail;
	}
	
	@Override
	public boolean hasNext()
	{
		return !this.returned || this.head.hasNext();
	}
	
	@Override
	public E next()
	{
		if (this.head.hasNext())
		{
			return this.head.next();
		}
		if (!this.returned)
		{
			this.returned = true;
			return this.tail;
		}
		throw new NoSuchElementException();
	}
}
