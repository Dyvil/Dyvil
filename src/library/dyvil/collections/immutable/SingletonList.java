package dyvil.collections.immutable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.collections.SingletonIterator;
import dyvil.collections.mutable.MutableList;
import dyvil.lang.Collection;

public class SingletonList<E> implements ImmutableList<E>
{
	private E	element;
	
	public SingletonList(E element)
	{
		this.element = element;
	}
	
	@Override
	public int size()
	{
		return 1;
	}
	
	@Override
	public boolean isEmpty()
	{
		return false;
	}
	
	@Override
	public Iterator<E> iterator()
	{
		return new SingletonIterator<E>(this.element);
	}
	
	@Override
	public Spliterator<E> spliterator()
	{
		return null; // FIXME
	}
	
	@Override
	public void forEach(Consumer<? super E> action)
	{
		action.accept(this.element);
	}
	
	@Override
	public boolean $qmark(Object element)
	{
		return Objects.equals(element, this.element);
	}
	
	@Override
	public E apply(int index)
	{
		if (index == 0)
		{
			return this.element;
		}
		throw new IndexOutOfBoundsException("Index out of bounds for Singleton List");
	}
	
	@Override
	public E get(int index)
	{
		return index == 0 ? this.element : null;
	}
	
	@Override
	public ImmutableList<E> slice(int startIndex, int length)
	{
		if (startIndex > 0 || length > 0)
		{
			throw new IndexOutOfBoundsException("Slice out of range for Empty List");
		}
		return this;
	}
	
	@Override
	public int indexOf(E element)
	{
		return Objects.equals(this.element, element) ? 0 : -1;
	}
	
	@Override
	public int lastIndexOf(E element)
	{
		return Objects.equals(this.element, element) ? 0 : -1;
	}
	
	@Override
	public ImmutableList<E> $plus(E element)
	{
		return ImmutableList.apply(this.element, element);
	}
	
	@Override
	public ImmutableList<? extends E> $plus(Collection<? extends E> collection)
	{
		return null; // FIXME
	}
	
	@Override
	public ImmutableList<E> $minus(E element)
	{
		if (Objects.equals(this.element, element))
		{
			return ImmutableList.apply();
		}
		return ImmutableList.apply(this.element);
	}
	
	@Override
	public ImmutableList<? extends E> $minus(Collection<? extends E> collection)
	{
		if (collection.$qmark(this.element))
		{
			return ImmutableList.apply();
		}
		return ImmutableList.apply(this.element);
	}
	
	@Override
	public ImmutableList<? extends E> $amp(Collection<? extends E> collection)
	{
		if (!collection.$qmark(this.element))
		{
			return ImmutableList.apply();
		}
		return this;
	}
	
	@Override
	public <R> ImmutableList<R> mapped(Function<? super E, ? extends R> mapper)
	{
		return ImmutableList.apply(mapper.apply(this.element));
	}
	
	@Override
	public <R> ImmutableList<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		return ImmutableList.apply(mapper.apply(this.element));
	}
	
	@Override
	public ImmutableList<E> filtered(Predicate<? super E> condition)
	{
		if (condition.test(this.element))
		{
			return ImmutableList.apply(this.element);
		}
		return ImmutableList.apply();
	}
	
	@Override
	public ImmutableList<E> sorted()
	{
		return ImmutableList.apply(this.element);
	}
	
	@Override
	public ImmutableList<E> sorted(Comparator<? super E> comparator)
	{
		return ImmutableList.apply(this.element);
	}
	
	@Override
	public ImmutableList<E> copy()
	{
		return ImmutableList.apply(this.element);
	}
	
	@Override
	public MutableList<E> mutable()
	{
		return MutableList.apply(this.element);
	}
}
