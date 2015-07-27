package dyvil.collection.iterator;

import java.util.Iterator;
import java.util.function.Consumer;

import dyvil.util.ImmutableException;

public class ImmutableIterator<E> implements Iterator<E>
{
	private final Iterator<E> iterator;
	
	public ImmutableIterator(Iterator<E> iterator)
	{
		this.iterator = iterator;
	}
	
	@Override
	public boolean hasNext()
	{
		return this.iterator.hasNext();
	}
	
	@Override
	public E next()
	{
		return this.iterator.next();
	}
	
	@Override
	public void forEachRemaining(Consumer<? super E> action)
	{
		this.iterator.forEachRemaining(action);
	}
	
	@Override
	public void remove()
	{
		throw new ImmutableException("remove() on Immutable Iterator");
	}
	
	@Override
	public String toString()
	{
		return "view " + this.iterator.toString();
	}
}
