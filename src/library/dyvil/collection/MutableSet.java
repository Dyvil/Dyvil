package dyvil.collection;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.lang.Collection;
import dyvil.lang.Set;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import dyvil.collection.mutable.HashMap;
import dyvil.collection.mutable.MapBasedSet;

@NilConvertible
@ArrayConvertible
public interface MutableSet<E> extends Set<E>, MutableCollection<E>
{
	public static <E> MutableSet<E> apply()
	{
		return new MapBasedSet<E>(new HashMap<E, Object>());
	}
	
	public static <E> MutableSet<E> apply(E... elements)
	{
		HashMap<E, Object> hashMap = new HashMap(elements.length);
		for (E element : elements)
		{
			hashMap.put(element, VALUE);
		}
		return new MapBasedSet<E>(hashMap);
	}
	
	// Accessors
	
	@Override
	public int size();
	
	@Override
	public Iterator<E> iterator();
	
	@Override
	public boolean contains(Object element);
	
	// Non-mutating Operations
	
	@Override
	public default MutableSet<E> $plus(E element)
	{
		MutableSet<E> copy = this.copy();
		copy.$plus$eq(element);
		return copy;
	}
	
	@Override
	public default MutableSet<E> $minus(Object element)
	{
		MutableSet<E> copy = this.copy();
		copy.$minus$eq(element);
		return copy;
	}
	
	@Override
	public default MutableSet<? extends E> $minus$minus(Collection<?> collection)
	{
		MutableSet<E> copy = this.copy();
		copy.$minus$minus$eq(collection);
		return copy;
	}
	
	@Override
	public default MutableSet<? extends E> $plus$plus(Collection<? extends E> collection)
	{
		MutableSet<E> copy = this.copy();
		copy.$bar$eq(collection);
		return copy;
	}
	
	@Override
	public default MutableSet<? extends E> $amp(Collection<? extends E> collection)
	{
		MutableSet<E> copy = this.copy();
		copy.$amp$eq(collection);
		return copy;
	}
	
	@Override
	public default MutableSet<? extends E> $bar(Collection<? extends E> collection)
	{
		MutableSet<E> copy = this.copy();
		copy.$bar$eq(collection);
		return copy;
	}
	
	@Override
	public default MutableSet<? extends E> $up(Collection<? extends E> collection)
	{
		MutableSet<E> copy = this.copy();
		copy.$up$eq(collection);
		return copy;
	}
	
	@Override
	public default <R> MutableSet<R> mapped(Function<? super E, ? extends R> mapper) {
		MutableSet<R> copy = (MutableSet<R>) this.copy();
		copy.map((Function) mapper);
		return copy;
	}
	
	@Override
	public default <R> MutableSet<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		MutableSet<R> copy = (MutableSet<R>) this.copy();
		copy.flatMap((Function) mapper);
		return copy;
	}
	
	@Override
	public default MutableSet<E> filtered(Predicate<? super E> condition)
	{
		MutableSet<E> copy = this.copy();
		copy.filter(condition);
		return copy;
	}
	
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
	public default MutableSet<E> mutableCopy()
	{
		return this.copy();
	}
	
	@Override
	public ImmutableSet<E> immutable();
	
	@Override
	public default ImmutableSet<E> immutableCopy()
	{
		return this.immutable();
	}
}
