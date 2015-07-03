package dyvil.collection;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.lang.Collection;
import dyvil.lang.List;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import dyvil.collection.mutable.ArrayList;

@NilConvertible
@ArrayConvertible
public interface MutableList<E> extends List<E>, MutableCollection<E>
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
	public boolean contains(Object element);
	
	@Override
	public E subscript(int index);
	
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
	public MutableList<? extends E> $minus$minus(Collection<?> collection);
	
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
	
	@Override
	public MutableList<E> distinct();
	
	@Override
	public MutableList<E> distinct(Comparator<? super E> comparator);
	
	// Mutating Operations
	
	@Override
	public void $plus$eq(E element);
	
	@Override
	public void resize(int newLength);
	
	@Override
	public void subscript_$eq(int index, E element);
	
	@Override
	public E set(int index, E element);
	
	@Override
	public E add(int index, E element);
	
	@Override
	public void removeAt(int index);
	
	@Override
	public boolean remove(Object element);
	
	@Override
	public void clear();
	
	@Override
	public void filter(Predicate<? super E> condition);
	
	@Override
	public void map(Function<? super E, ? extends E> mapper);
	
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
	public default MutableList<E> mutableCopy()
	{
		return this.copy();
	}
	
	@Override
	public ImmutableList<E> immutable();
	
	@Override
	public default ImmutableList<E> immutableCopy()
	{
		return this.immutable();
	}
}
