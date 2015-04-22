package dyvil.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

public final class SingletonIterator<E> implements Iterator<E>
{
	private boolean	returned;
	private E		e;
	
	public SingletonIterator(E e)
	{
		this.e = e;
	}
	
	@Override
	public boolean hasNext()
	{
		return !this.returned;
	}
	
	@Override
	public E next()
	{
		if (!this.returned)
		{
			this.returned = true;
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
		if (!this.returned)
		{
			action.accept(this.e);
			this.returned = true;
		}
	}
}
