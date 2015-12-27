package dyvil.collection.immutable;

import dyvil.collection.*;
import dyvil.collection.impl.AbstractMapBasedSet;
import dyvil.collection.mutable.HashMap;
import dyvil.annotation.Immutable;

import java.util.Collections;
import java.util.function.Function;
import java.util.function.Predicate;

@Immutable
public class MapBasedSet<E> extends AbstractMapBasedSet<E> implements ImmutableSet<E>
{
	private static final long serialVersionUID = 2820007412138106503L;
	
	protected ImmutableMap<E, Object> map;
	
	public MapBasedSet(ImmutableMap<E, ? extends Object> map)
	{
		this.map = (ImmutableMap<E, Object>) map;
	}
	
	@Override
	protected Map<E, Object> map()
	{
		return this.map;
	}
	
	@Override
	public ImmutableSet<E> $plus(E element)
	{
		return new MapBasedSet(this.map.$plus(element, VALUE));
	}
	
	@Override
	public ImmutableSet<E> $minus(Object element)
	{
		return new MapBasedSet(this.map.$minus$at(element));
	}
	
	@Override
	public ImmutableSet<? extends E> $minus$minus(Collection<?> collection)
	{
		MutableMap<E, Object> map = new HashMap();
		for (Entry<E, Object> entry : this.map)
		{
			E element = entry.getKey();
			if (!collection.contains(element))
			{
				map.subscript_$eq(element, VALUE);
			}
		}
		return new MapBasedSet(map.immutable());
	}
	
	@Override
	public ImmutableSet<? extends E> $amp(Collection<? extends E> collection)
	{
		MutableMap<E, Object> map = new HashMap();
		for (Entry<E, Object> entry : this.map)
		{
			E element = entry.getKey();
			if (collection.contains(element))
			{
				map.subscript_$eq(element, VALUE);
			}
		}
		return new MapBasedSet(map.immutable());
	}
	
	@Override
	public ImmutableSet<? extends E> $bar(Collection<? extends E> collection)
	{
		MutableMap<E, Object> map = this.map.mutableCopy();
		for (E element : collection)
		{
			map.subscript_$eq(element, VALUE);
		}
		return new MapBasedSet(map.immutable());
	}
	
	@Override
	public ImmutableSet<? extends E> $up(Collection<? extends E> collection)
	{
		MutableMap<E, Object> map = new HashMap();
		for (Entry<E, Object> entry : this.map)
		{
			E element = entry.getKey();
			if (!collection.contains(element))
			{
				map.subscript_$eq(element, VALUE);
			}
		}
		for (E element : collection)
		{
			if (!this.contains(element))
			{
				map.subscript_$eq(element, VALUE);
			}
		}
		return new MapBasedSet(map.immutable());
	}
	
	@Override
	public <R> ImmutableSet<R> mapped(Function<? super E, ? extends R> mapper)
	{
		MutableMap<R, Object> map = new HashMap();
		for (Entry<E, Object> entry : this.map)
		{
			map.subscript_$eq(mapper.apply(entry.getKey()), VALUE);
		}
		return new MapBasedSet(map.immutable());
	}
	
	@Override
	public <R> ImmutableSet<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		MutableMap<R, Object> map = new HashMap();
		for (Entry<E, Object> entry : this.map)
		{
			for (R element : mapper.apply(entry.getKey()))
			{
				map.subscript_$eq(element, VALUE);
			}
		}
		return new MapBasedSet(map.immutable());
	}
	
	@Override
	public ImmutableSet<E> filtered(Predicate<? super E> condition)
	{
		MutableMap<E, Object> map = new HashMap();
		for (Entry<E, Object> entry : this.map)
		{
			E element = entry.getKey();
			if (condition.test(element))
			{
				map.subscript_$eq(element, VALUE);
			}
		}
		return new MapBasedSet(map.immutable());
	}
	
	@Override
	public ImmutableSet<E> copy()
	{
		return new MapBasedSet(this.map.copy());
	}
	
	@Override
	public MutableSet<E> mutable()
	{
		return new dyvil.collection.mutable.MapBasedSet<E>(this.map.mutable());
	}
	
	@Override
	public java.util.Set<E> toJava()
	{
		return Collections.unmodifiableSet(super.toJava());
	}
}
