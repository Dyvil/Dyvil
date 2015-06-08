package dyvil.collection;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import dyvil.collection.mutable.ArrayList;
import dyvil.lang.Collection;
import dyvil.lang.List;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

@NilConvertible
@ArrayConvertible
public interface MutableList<E> extends MutableCollection<E>, List<E>
{
	public static <E> MutableList<E> apply()
	{
		return new ArrayList();
	}
	
	public static <E> MutableList<E> apply(E element)
	{
		return new ArrayList(new Object[] { element }, 1, true);
	}
	
	public static <E> MutableList<E> apply(E e1, E e2)
	{
		return new ArrayList(new Object[] { e1, e2 }, 2, true);
	}
	
	public static <E> MutableList<E> apply(E e1, E e2, E e3)
	{
		return new ArrayList(new Object[] { e1, e2, e3 }, 3, true);
	}
	
	public static <E> MutableList<E> apply(E... elements)
	{
		return new ArrayList(elements, true);
	}
	
	// Accessors
	
	@Override
	public int size();
	
	@Override
	public Iterator<E> iterator();
	
	@Override
	public boolean $qmark(Object element);
	
	@Override
	public E apply(int index);
	
	@Override
	public E get(int index);
	
	// Non-mutating Operations
	
	@Override
	public MutableList<E> subList(int startIndex, int length);
	
	@Override
	public MutableList<E> $plus(E element);
	
	@Override
	public MutableList<? extends E> $plus$plus(Collection<? extends E> collection);
	
	@Override
	public MutableList<E> $minus(Object element);
	
	@Override
	public MutableList<? extends E> $minus$minus(Collection<? extends E> collection);
	
	@Override
	public MutableList<? extends E> $amp(Collection<? extends E> collection);
	
	@Override
	public MutableList<? extends E> $bar(Collection<? extends E> collection);
	
	@Override
	public MutableList<? extends E> $up(Collection<? extends E> collection);
	
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
	
	@Override
	public MutableList<E> distinct();
	
	@Override
	public MutableList<E> distinct(Comparator<? super E> comparator);
	
	// Mutating Operations
	
	@Override
	public void resize(int newLength);
	
	@Override
	public default void ensureCapacity(int minSize)
	{
	}
	
	@Override
	public void update(int index, E element);
	
	@Override
	public E set(int index, E element);
	
	@Override
	public void insert(int index, E element);
	
	@Override
	public boolean add(E element);
	
	@Override
	public E add(int index, E element);
	
	@Override
	public void removeAt(int index);
	
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
	public void distinguish();
	
	@Override
	public void distinguish(Comparator<? super E> comparator);
	
	// Search Operations
	
	@Override
	public int indexOf(Object element);
	
	@Override
	public int lastIndexOf(Object element);
	
	// toArray
	
	@Override
	public void toArray(int index, Object[] store);
	
	// Copying
	
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
