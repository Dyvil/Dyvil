package dyvil.collection;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import dyvil.lang.Collection;
import dyvil.lang.Set;

public interface MutableSet<E> extends Set<E>, MutableCollection<E>
{
	// Accessors
	
	@Override
	public int size();
	
	@Override
	public Iterator<E> iterator();
	
	@Override
	public boolean $qmark(Object element);
	
	// Non-mutating Operations
	
	@Override
	public MutableSet<E> $plus(E element);
	
	@Override
	public MutableSet<E> $minus(Object element);
	
	@Override
	public MutableSet<? extends E> $minus$minus(Collection<? extends E> collection);
	
	@Override
	public default MutableSet<? extends E> $plus$plus(Collection<? extends E> collection)
	{
		return this.$bar(collection);
	}
	
	@Override
	public MutableSet<? extends E> $amp(Collection<? extends E> collection);
	
	@Override
	public MutableSet<? extends E> $bar(Collection<? extends E> collection);
	
	@Override
	public MutableSet<? extends E> $up(Collection<? extends E> collection);
	
	@Override
	public <R> MutableSet<R> mapped(Function<? super E, ? extends R> mapper);
	
	@Override
	public <R> MutableSet<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper);
	
	@Override
	public MutableSet<E> filtered(Predicate<? super E> condition);
	
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
	public MutableSet<E> copy();
	
	@Override
	public default MutableSet<E> mutable()
	{
		return this;
	}
	
	@Override
	public ImmutableSet<E> immutable();
}
