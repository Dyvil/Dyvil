package dyvil.collections.mutable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import dyvil.collections.immutable.ImmutableList;
import dyvil.lang.Collection;
import dyvil.lang.List;

public interface MutableList<E> extends MutableCollection<E>, List<E>
{
	public static <E> MutableList<E> apply()
	{
		return null; // FIXME
	}
	
	public static <E> MutableList<E> apply(E element)
	{
		return null; // FIXME
	}
	
	public static <E> MutableList<E> apply(E[] array)
	{
		return null; // FIXME
	}
	
	@Override
	public int size();
	
	@Override
	public boolean isEmpty();
	
	@Override
	public Iterator<E> iterator();
	
	@Override
	public Spliterator<E> spliterator();
	
	@Override
	public void forEach(Consumer<? super E> action);
	
	@Override
	public boolean $qmark(Object element);
	
	@Override
	public E apply(int index);
	
	@Override
	public E get(int index);
	
	@Override
	public MutableList<E> slice(int startIndex, int length);
	
	@Override
	public MutableList<E> $plus(E element);
	
	@Override
	public MutableList<? extends E> $plus(Collection<? extends E> collection);
	
	@Override
	public MutableList<E> $minus(E element);
	
	@Override
	public MutableList<? extends E> $minus(Collection<? extends E> collection);
	
	@Override
	public MutableList<? extends E> $amp(Collection<? extends E> collection);
	
	@Override
	public <R> MutableList<R> mapped(Function<? super E, ? extends R> mapper);
	
	@Override
	public <R> MutableList<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper);
	
	@Override
	public MutableList<E> filtered(Predicate<? super E> condition);
	
	@Override
	public MutableList<E> sorted();
	
	@Override
	public MutableList<E> sorted(Comparator<? super E> comparator);
	
	// Mutating Functions
	
	@Override
	public void $plus$eq(E element);
	
	@Override
	public void $plus$eq(Collection<? extends E> collection);
	
	@Override
	public void $minus$eq(E element);
	
	@Override
	public void $minus$eq(Collection<? extends E> collection);
	
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
	public void sort();
	
	@Override
	public void sort(Comparator<? super E> comparator);
	
	@Override
	public void resize(int newLength);
	
	@Override
	public void update(int index, E element);
	
	@Override
	public E set(int index, E element);
	
	@Override
	public void add(int index, E element);
	
	@Override
	public void remove(E element);
	
	@Override
	public void removeAt(int index);
	
	@Override
	public int indexOf(E element);
	
	@Override
	public int lastIndexOf(E element);
	
	@Override
	public MutableList<E> copy();
	
	@Override
	public default MutableList<E> mutable()
	{
		return this;
	}
	
	@Override
	public ImmutableList<E> immutable();
}
