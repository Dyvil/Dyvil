package dyvil.collection;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import dyvil.collection.mutable.ArraySet;
import dyvil.collection.mutable.HashSet;
import dyvil.collection.view.SetView;

@NilConvertible
@ArrayConvertible
public interface MutableSet<E> extends Set<E>, MutableCollection<E>
{
	static <E> MutableSet<E> apply()
	{
		return new HashSet<E>();
	}
	
	static <E> MutableSet<E> apply(E... elements)
	{
		return new ArraySet(elements, true);
	}
	
	static <E> MutableSet<E> fromArray(E... elements)
	{
		return new ArraySet(elements);
	}
	
	// Accessors
	
	@Override
	int size();
	
	@Override
	Iterator<E> iterator();
	
	// Non-mutating Operations
	
	@Override
	default MutableSet<E> $plus(E element)
	{
		MutableSet<E> copy = this.copy();
		copy.$plus$eq(element);
		return copy;
	}
	
	@Override
	default MutableSet<E> $minus(Object element)
	{
		MutableSet<E> copy = this.copy();
		copy.$minus$eq(element);
		return copy;
	}
	
	@Override
	default MutableSet<? extends E> $minus$minus(Collection<?> collection)
	{
		MutableSet<E> copy = this.copy();
		copy.$minus$minus$eq(collection);
		return copy;
	}
	
	@Override
	default MutableSet<? extends E> $plus$plus(Collection<? extends E> collection)
	{
		MutableSet<E> copy = this.copy();
		copy.$bar$eq(collection);
		return copy;
	}
	
	@Override
	default MutableSet<? extends E> $amp(Collection<? extends E> collection)
	{
		MutableSet<E> copy = this.copy();
		copy.$amp$eq(collection);
		return copy;
	}
	
	@Override
	default MutableSet<? extends E> $bar(Collection<? extends E> collection)
	{
		MutableSet<E> copy = this.copy();
		copy.$bar$eq(collection);
		return copy;
	}
	
	@Override
	default MutableSet<? extends E> $up(Collection<? extends E> collection)
	{
		MutableSet<E> copy = this.copy();
		copy.$up$eq(collection);
		return copy;
	}
	
	@Override
	default <R> MutableSet<R> mapped(Function<? super E, ? extends R> mapper)
	{
		MutableSet<R> copy = (MutableSet<R>) this.copy();
		copy.map((Function) mapper);
		return copy;
	}
	
	@Override
	default <R> MutableSet<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		MutableSet<R> copy = (MutableSet<R>) this.copy();
		copy.flatMap((Function) mapper);
		return copy;
	}
	
	@Override
	default MutableSet<E> filtered(Predicate<? super E> condition)
	{
		MutableSet<E> copy = this.copy();
		copy.filter(condition);
		return copy;
	}
	
	// Mutating Operations
	
	@Override
	void clear();
	
	@Override
	boolean add(E element);
	
	@Override
	boolean remove(Object element);
	
	@Override
	void map(Function<? super E, ? extends E> mapper);
	
	@Override
	void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper);
	
	// Copying
	
	@Override
	MutableSet<E> copy();
	
	@Override
	default MutableSet<E> mutable()
	{
		return this;
	}
	
	@Override
	default MutableSet<E> mutableCopy()
	{
		return this.copy();
	}
	
	@Override
	<R> MutableSet<R> emptyCopy();
	
	@Override
	ImmutableSet<E> immutable();
	
	@Override
	default ImmutableSet<E> immutableCopy()
	{
		return this.immutable();
	}
	
	@Override
	default ImmutableSet<E> view()
	{
		return new SetView(this);
	}
}
