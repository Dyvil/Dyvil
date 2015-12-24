package dyvil.collection.iterator;

import dyvil.annotation.Mutating;
import dyvil.annotation.Immutable;
import dyvil.util.ImmutableException;

import java.util.Iterator;
import java.util.function.Consumer;

@Immutable
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
	@Mutating
	public void remove()
	{
		throw new ImmutableException("remove() on Immutable Iterator");
	}
	
	@Override
	public String toString()
	{
		return this.iterator.toString();
	}
}
