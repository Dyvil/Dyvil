package dyvil.lang;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import dyvil.collections.immutable.ImmutableList;
import dyvil.collections.mutable.MutableList;

public interface List<E> extends Collection<E>
{
	// Simple getters
	
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
	
	public E apply(int index);
	
	public E get(int index);
	
	// Non-mutating Operations
	
	public List<E> slice(int startIndex, int length);
	
	@Override
	public List<E> $plus(E element);
	
	@Override
	public List<? extends E> $plus(Collection<? extends E> collection);
	
	@Override
	public List<E> $minus(E element);
	
	@Override
	public List<? extends E> $minus(Collection<? extends E> collection);
	
	@Override
	public List<? extends E> $amp(Collection<? extends E> collection);
	
	@Override
	public <R> List<R> mapped(Function<? super E, ? extends R> mapper);
	
	@Override
	public <R> List<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper);
	
	@Override
	public List<E> filtered(Predicate<? super E> condition);
	
	@Override
	public List<E> sorted();
	
	@Override
	public List<E> sorted(Comparator<? super E> comparator);
	
	// Mutating Operations
	
	public void resize(int newLength);

	public void update(int index, E element);

	public E set(int index, E element);

	public void add(int index, E element);

	public void remove(E element);

	public void removeAt(int index);

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
	
	// Search Operations
	
	public int indexOf(E element);
	
	public int lastIndexOf(E element);
	
	// Copying
	
	@Override
	public List<E> copy();
	
	@Override
	public MutableList<E> mutable();
	
	@Override
	public ImmutableList<E> immutable();
}
