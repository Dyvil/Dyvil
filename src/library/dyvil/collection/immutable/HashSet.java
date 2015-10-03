package dyvil.collection.immutable;

import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.lang.literal.ArrayConvertible;

import dyvil.collection.Collection;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.Set;
import dyvil.collection.impl.AbstractHashSet;
import dyvil.math.MathUtils;

import static dyvil.collection.impl.AbstractHashMap.DEFAULT_CAPACITY;

@ArrayConvertible
public class HashSet<E> extends AbstractHashSet<E>implements ImmutableSet<E>
{
	public static <E> HashSet<E> apply(E... elements)
	{
		return new HashSet(elements);
	}
	
	protected HashSet()
	{
		this(DEFAULT_CAPACITY);
	}
	
	public HashSet(int capacity)
	{
		this.elements = new HashElement[MathUtils.powerOfTwo(capacity)];
	}
	
	public HashSet(Collection<E> collection)
	{
		super(collection);
	}
	
	public HashSet(Set<E> set)
	{
		super(set);
	}
	
	public HashSet(AbstractHashSet<E> set)
	{
		super(set);
	}
	
	public HashSet(E... elements)
	{
		super(elements);
	}
	
	@Override
	public ImmutableSet<E> $plus(E element)
	{
		HashSet<E> newSet = new HashSet<E>(this);
		newSet.addInternal(element);
		return newSet;
	}
	
	@Override
	public ImmutableSet<E> $minus(Object element)
	{
		HashSet<E> newSet = new HashSet<E>(this.size);
		
		for (E element1 : this)
		{
			if (element1 != element && (element == null || !element.equals(element1)))
			{
				newSet.addInternal(element1);
			}
		}
		return newSet;
	}
	
	@Override
	public ImmutableSet<? extends E> $minus$minus(Collection<?> collection)
	{
		HashSet<E> newSet = new HashSet<E>(this.size);
		
		for (E element1 : this)
		{
			if (!collection.contains(element1))
			{
				newSet.addInternal(element1);
			}
		}
		
		return newSet;
	}
	
	@Override
	public ImmutableSet<? extends E> $amp(Collection<? extends E> collection)
	{
		HashSet<E> newSet = new HashSet<E>(this.size);
		
		for (E element1 : this)
		{
			if (collection.contains(element1))
			{
				newSet.addInternal(element1);
			}
		}
		
		return newSet;
	}
	
	@Override
	public ImmutableSet<? extends E> $bar(Collection<? extends E> collection)
	{
		HashSet<E> newSet = new HashSet<E>(this);
		for (E element : collection)
		{
			newSet.addInternal(element);
		}
		return newSet;
	}
	
	@Override
	public ImmutableSet<? extends E> $up(Collection<? extends E> collection)
	{
		HashSet<E> newSet = new HashSet<E>(this.size + collection.size());
		
		for (E element : this)
		{
			if (!collection.contains(element))
			{
				newSet.addInternal(element);
			}
		}
		
		for (E element : collection)
		{
			if (!this.contains(element))
			{
				newSet.addInternal(element);
			}
		}
		return newSet;
	}
	
	@Override
	public <R> ImmutableSet<R> mapped(Function<? super E, ? extends R> mapper)
	{
		HashSet<R> newSet = new HashSet<R>(this.size);
		
		for (E element : this)
		{
			newSet.addInternal(mapper.apply(element));
		}
		return newSet;
	}
	
	@Override
	public <R> ImmutableSet<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		HashSet<R> newSet = new HashSet<R>(this.size << 2);
		
		for (E element : this)
		{
			for (R newElement : mapper.apply(element))
			{
				newSet.addInternal(newElement);
			}
		}
		return newSet;
	}
	
	@Override
	public ImmutableSet<E> filtered(Predicate<? super E> condition)
	{
		HashSet<E> newSet = new HashSet<E>(this.size);
		
		for (E element : this)
		{
			if (condition.test(element))
			{
				newSet.addInternal(element);
			}
		}
		return newSet;
	}
	
	@Override
	public ImmutableSet<E> copy()
	{
		return new HashSet<E>(this);
	}
	
	@Override
	public MutableSet<E> mutable()
	{
		return new dyvil.collection.mutable.HashSet<E>(this);
	}
	
	@Override
	public java.util.Set<E> toJava()
	{
		java.util.Set<E> set = new java.util.HashSet<E>();
		for (E element : this)
		{
			set.add(element);
		}
		return set;
	}
	
}
