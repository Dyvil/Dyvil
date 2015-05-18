package dyvil.lang;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;

public interface Set<E> extends Collection<E>
{
	// Accessors
	
	@Override
	public int size();
	
	@Override
	public Iterator<E> iterator();
	
	@Override
	public default Spliterator<E> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), Spliterator.DISTINCT);
	}
	
	@Override
	public boolean $qmark(Object element);
	
	// Non-mutating Operations
	
	@Override
	public Set<E> $plus(E element);
	
	/**
	 * {@inheritDoc} This operator represents the 'union' Set operation and
	 * delegates to {@link #$bar(Collection)}.
	 */
	@Override
	public default Set<? extends E> $plus$plus(Collection<? extends E> collection)
	{
		return this.$bar(collection);
	}
	
	@Override
	public Set<E> $minus(Object element);
	
	/**
	 * {@inheritDoc} This operator represents the 'subtract' Set operation.
	 */
	@Override
	public Set<? extends E> $minus$minus(Collection<? extends E> collection);
	
	/**
	 * {@inheritDoc} This operator represents the 'intersect' Set operation.
	 */
	@Override
	public Set<? extends E> $amp(Collection<? extends E> collection);
	
	/**
	 * {@inheritDoc} This operator represents the 'union' Set operation.
	 */
	@Override
	public Set<? extends E> $bar(Collection<? extends E> collection);
	
	/**
	 * {@inheritDoc} This operator represents the 'exclusive OR' Set operation.
	 */
	@Override
	public Set<? extends E> $up(Collection<? extends E> collection);
	
	@Override
	public <R> Set<R> mapped(Function<? super E, ? extends R> mapper);
	
	@Override
	public <R> Set<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper);
	
	@Override
	public Set<E> filtered(Predicate<? super E> condition);
	
	// Mutating Operations
	
	@Override
	public E add(E element);
	
	@Override
	public boolean remove(E element);
	
	@Override
	public void $amp$eq(Collection<? extends E> collection);
	
	@Override
	public void $bar$eq(Collection<? extends E> collection);
	
	@Override
	public void $up$eq(Collection<? extends E> collection);
	
	@Override
	public void clear();
	
	@Override
	public void map(UnaryOperator<E> mapper);
	
	@Override
	public void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper);
	
	@Override
	public void filter(Predicate<? super E> condition);
	
	// toArray
	
	@Override
	public void toArray(int index, Object[] store);
	
	// Copying
	
	@Override
	public Set<E> copy();
	
	@Override
	public MutableSet<E> mutable();
	
	@Override
	public ImmutableSet<E> immutable();
}
