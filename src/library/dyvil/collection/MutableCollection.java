package dyvil.collection;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import dyvil.lang.Collection;
import dyvil.lang.literal.NilConvertible;

@NilConvertible
public interface MutableCollection<E> extends Collection<E>
{
	public static <E> MutableCollection<E> apply()
	{
		return MutableList.apply();
	}
	
	@Override
	public int size();
	
	@Override
	public boolean isEmpty();
	
	@Override
	public Iterator<E> iterator();
	
	@Override
	public default Spliterator<E> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), 0);
	}
	
	@Override
	public void forEach(Consumer<? super E> action);
	
	@Override
	public boolean $qmark(Object element);
	
	@Override
	public MutableCollection<E> $plus(E element);
	
	@Override
	public MutableCollection<? extends E> $plus$plus(Collection<? extends E> collection);
	
	@Override
	public MutableCollection<E> $minus(E element);
	
	@Override
	public MutableCollection<? extends E> $minus$minus(Collection<? extends E> collection);
	
	@Override
	public MutableCollection<? extends E> $amp(Collection<? extends E> collection);
	
	@Override
	public <R> MutableCollection<R> mapped(Function<? super E, ? extends R> mapper);
	
	@Override
	public <R> MutableCollection<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper);
	
	@Override
	public MutableCollection<E> filtered(Predicate<? super E> condition);
	
	@Override
	public MutableCollection<E> sorted();
	
	@Override
	public MutableCollection<E> sorted(Comparator<? super E> comparator);
	
	// Mutating Functions
	
	@Override
	public void $plus$eq(E element);
	
	@Override
	public void $plus$plus$eq(Collection<? extends E> collection);
	
	@Override
	public void $minus$eq(E element);
	
	@Override
	public void $minus$minus$eq(Collection<? extends E> collection);
	
	@Override
	public void $amp$eq(Collection<? extends E> collection);
	
	@Override
	public void clear();
	
	@Override
	public void filter(Predicate<? super E> condition);
	
	@Override
	public void map(UnaryOperator<E> mapper);
	
	@Override
	public void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper);
	
	@Override
	public MutableCollection<E> copy();
	
	@Override
	public default MutableCollection<E> mutable()
	{
		return this;
	}
	
	@Override
	public ImmutableCollection<E> immutable();
}
