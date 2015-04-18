package dyvil.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

public final class SingletonIterator<E> implements Iterator<E>
{
	private boolean	hasNext	= true;
	private E		e;
	
	public SingletonIterator(E e)
	{
		this.e = e;
	}
	
	@Override
	public boolean hasNext()
	{
		return this.hasNext;
	}
	
	@Override
	public E next()
	{
		if (this.hasNext)
		{
			this.hasNext = false;
			return this.e;
		}
		throw new NoSuchElementException();
	}
	
	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void forEachRemaining(Consumer<? super E> action)
	{
		Objects.requireNonNull(action);
		if (this.hasNext)
		{
			action.accept(this.e);
			this.hasNext = false;
		}
	}
}
