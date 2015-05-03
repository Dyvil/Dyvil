package dyvil.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

import dyvil.lang.literal.TupleConvertible;

@TupleConvertible
public final class SingletonIterator<E> implements Iterator<E>
{
	private boolean	returned;
	private E		element;
	
	public static <E> SingletonIterator<E> apply(E element)
	{
		return new SingletonIterator(element);
	}
	
	public SingletonIterator(E element)
	{
		this.element = element;
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
			return this.element;
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
			action.accept(this.element);
			this.returned = true;
		}
	}
}
