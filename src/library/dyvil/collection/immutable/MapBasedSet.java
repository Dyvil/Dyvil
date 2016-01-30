package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.collection.*;
import dyvil.collection.impl.AbstractMapBasedSet;

import java.util.Collections;
import java.util.function.Function;
import java.util.function.Predicate;

@Immutable
public class MapBasedSet<E> extends AbstractMapBasedSet<E> implements ImmutableSet<E>
{
	private static final long serialVersionUID = 2820007412138106503L;
	
	protected ImmutableMap<E, Boolean> map;
	
	public MapBasedSet(ImmutableMap<E, Boolean> map)
	{
		this.map = map;
	}
	
	@Override
	protected Map<E, Boolean> map()
	{
		return this.map;
	}
	
	@Override
	public ImmutableSet<E> $plus(E element)
	{
		return new MapBasedSet<>(this.map.$plus(element, true));
	}
	
	@Override
	public ImmutableSet<E> $minus(Object element)
	{
		return new MapBasedSet<>(this.map.$minus$at(element));
	}
	
	@Override
	public ImmutableSet<? extends E> $minus$minus(Collection<?> collection)
	{
		ImmutableMap.Builder<E, Boolean> builder = this.map.immutableBuilder();
		for (Entry<E, ?> entry : this.map)
		{
			E element = entry.getKey();
			if (!collection.contains(element))
			{
				builder.put(element, true);
			}
		}
		return new MapBasedSet<>(builder.build());
	}
	
	@Override
	public ImmutableSet<? extends E> $amp(Collection<? extends E> collection)
	{
		ImmutableMap.Builder<E, Boolean> builder = this.map.immutableBuilder();
		for (Entry<E, ?> entry : this.map)
		{
			E element = entry.getKey();
			if (collection.contains(element))
			{
				builder.put(element, true);
			}
		}
		return new MapBasedSet<>(builder.build());
	}
	
	@Override
	public ImmutableSet<? extends E> $bar(Collection<? extends E> collection)
	{
		ImmutableMap.Builder<E, Boolean> builder = this.map.immutableBuilder();
		builder.putAll(this.map);
		for (E element : collection)
		{
			builder.put(element, true);
		}
		return new MapBasedSet<>(builder.build());
	}
	
	@Override
	public ImmutableSet<? extends E> $up(Collection<? extends E> collection)
	{
		ImmutableMap.Builder<E, Boolean> builder = this.map.immutableBuilder();
		for (Entry<E, ?> entry : this.map)
		{
			E element = entry.getKey();
			if (!collection.contains(element))
			{
				builder.put(element, true);
			}
		}
		for (E element : collection)
		{
			if (!this.contains(element))
			{
				builder.put(element, true);
			}
		}
		return new MapBasedSet<>(builder.build());
	}
	
	@Override
	public <R> ImmutableSet<R> mapped(Function<? super E, ? extends R> mapper)
	{
		ImmutableMap.Builder<R, Boolean> builder = this.map.immutableBuilder();
		for (Entry<E, ?> entry : this.map)
		{
			builder.put(mapper.apply(entry.getKey()), true);
		}
		return new MapBasedSet<>(builder.build());
	}
	
	@Override
	public <R> ImmutableSet<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		ImmutableMap.Builder<R, Boolean> builder = this.map.immutableBuilder();
		for (Entry<E, ?> entry : this.map)
		{
			for (R element : mapper.apply(entry.getKey()))
			{
				builder.put(element, true);
			}
		}
		return new MapBasedSet<>(builder.build());
	}
	
	@Override
	public ImmutableSet<E> filtered(Predicate<? super E> condition)
	{
		ImmutableMap.Builder<E, Boolean> builder = this.map.immutableBuilder();
		for (Entry<E, ?> entry : this.map)
		{
			E element = entry.getKey();
			if (condition.test(element))
			{
				builder.put(element, true);
			}
		}
		return new MapBasedSet<>(builder.build());
	}
	
	@Override
	public ImmutableSet<E> copy()
	{
		return new MapBasedSet<>(this.map.copy());
	}
	
	@Override
	public MutableSet<E> mutable()
	{
		return new dyvil.collection.mutable.MapBasedSet<>(this.map.mutable());
	}
	
	@Override
	public java.util.Set<E> toJava()
	{
		return Collections.unmodifiableSet(super.toJava());
	}
}
