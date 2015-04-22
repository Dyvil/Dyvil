package dyvil.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class EmptyIterator<E> implements Iterator<E>
{
	static final EmptyIterator	instance	= new EmptyIterator();
	
	public static <E> EmptyIterator<E> apply()
	{
		return instance;
	}
	
	@Deprecated
	public EmptyIterator()
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
		throw new NoSuchElementException("Empty Iterator");
	}
	
	@Override
	public void remove()
	{
		throw new NoSuchElementException("Empty Iterator");
	}
	
	@Override
	public void forEachRemaining(Consumer<? super E> action)
	{
	}
}
