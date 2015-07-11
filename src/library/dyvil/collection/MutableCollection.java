package dyvil.collection;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

@NilConvertible
@ArrayConvertible
public interface MutableCollection<E> extends Collection<E>
{
	public static <E> MutableCollection<E> apply()
	{
		return MutableList.apply();
	}
	
	public static <E> MutableCollection<E> apply(E... elements)
	{
		return MutableList.apply(elements);
	}
	
	// Accessors
	
	@Override
	public int size();
	
	@Override
	public Iterator<E> iterator();
	
	// Non-mutating Operations
	
	@Override
	public MutableCollection<E> $plus(E element);
	
	@Override
	public MutableCollection<? extends E> $plus$plus(Collection<? extends E> collection);
	
	@Override
	public MutableCollection<E> $minus(Object element);
	
	@Override
	public MutableCollection<? extends E> $minus$minus(Collection<?> collection);
	
	@Override
	public MutableCollection<? extends E> $amp(Collection<? extends E> collection);
	
	@Override
	public <R> MutableCollection<R> mapped(Function<? super E, ? extends R> mapper);
	
	@Override
	public <R> MutableCollection<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper);
	
	@Override
	public MutableCollection<E> filtered(Predicate<? super E> condition);
	
	// Mutating Operations
	
	@Override
	public void clear();
	
	@Override
	public boolean add(E element);
	
	@Override
	public boolean remove(Object element);
	
	@Override
	public void map(Function<? super E, ? extends E> mapper);
	
	@Override
	public void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper);
	
	// Copying
	
	@Override
	public MutableCollection<E> copy();
	
	@Override
	public default MutableCollection<E> mutable()
	{
		return this;
	}
	
	@Override
	public default MutableCollection<E> mutableCopy()
	{
		return this.copy();
	}
	
	public <R> MutableCollection<R> emptyCopy();
	
	@Override
	public ImmutableCollection<E> immutable();
	
	@Override
	public default ImmutableCollection<E> immutableCopy()
	{
		return this.immutable();
	}
}
