package dyvil.collection.iterator;

import dyvil.annotation.Immutable;
import dyvil.annotation.Mutating;
import dyvil.lang.LiteralConvertible;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

@LiteralConvertible.FromNil
@Immutable
public final class EmptyIterator<E> implements Iterator<E>
{
	public static final EmptyIterator instance = new EmptyIterator();
	
	public static <E> EmptyIterator<E> apply()
	{
		return instance;
	}
	
	private EmptyIterator()
	{
	}
	
	@Override
	public boolean hasNext()
	{
		return false;
	}
	
	@Override
	public E next()
	{
		throw new NoSuchElementException("Empty Iterator has no elements to return");
	}
	
	@Override
	@Mutating
	public void remove()
	{
		throw new NoSuchElementException("Empty Iterator");
	}
	
	@Override
	public void forEachRemaining(Consumer<? super E> action)
	{
	}
	
	@Override
	public String toString()
	{
		return "EmptyIterator()";
	}
}
