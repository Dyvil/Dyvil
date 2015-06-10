package dyvil.collection.immutable;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.collection.ImmutableMap;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableMap;
import dyvil.collection.MutableSet;
import dyvil.collection.mutable.HashMap;
import dyvil.lang.Collection;
import dyvil.lang.Entry;
import dyvil.lang.Set;

public class MapBasedSet<E> implements ImmutableSet<E>
{
	protected ImmutableMap<E, Object>	map;
	
	public MapBasedSet(ImmutableMap<E, ? extends Object> map)
	{
		this.map = (ImmutableMap<E, Object>) map;
	}
	
	@Override
	public int size()
	{
		return this.map.size();
	}
	
	@Override
	public Iterator<E> iterator()
	{
		return this.map.keyIterator();
	}
	
	@Override
	public boolean $qmark(Object element)
	{
		return this.map.$qmark(element);
	}
	
	@Override
	public ImmutableSet<E> $plus(E element)
	{
		return new MapBasedSet(this.map.$plus(element, VALUE));
	}
	
	@Override
	public ImmutableSet<E> $minus(Object element)
	{
		return new MapBasedSet(this.map.$minus(element));
	}
	
	@Override
	public ImmutableSet<? extends E> $minus$minus(Collection<? extends E> collection)
	{
		MutableMap<E, Object> map = new HashMap();
		for (Entry<E, Object> entry : this.map)
		{
			E element = entry.getKey();
			if (!collection.$qmark(element))
			{
				map.update(element, VALUE);
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
			if (collection.$qmark(element))
			{
				map.update(element, VALUE);
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
			map.update(element, VALUE);
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
			if (!collection.$qmark(element))
			{
				map.update(element, VALUE);
			}
		}
		for (E element : collection)
		{
			if (!this.$qmark(element))
			{
				map.update(element, VALUE);
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
			map.update(mapper.apply(entry.getKey()), VALUE);
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
				map.update(element, VALUE);
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
				map.update(element, VALUE);
			}
		}
		return new MapBasedSet(map.immutable());
	}
	
	@Override
	public void toArray(int index, Object[] store)
	{
		for (Entry<E, Object> e : this.map)
		{
			store[index++] = e.getKey();
		}
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
	public String toString()
	{
		if (this.map.isEmpty())
		{
			return "[]";
		}
		
		StringBuilder builder = new StringBuilder("[");
		Iterator<Entry<E, Object>> iterator = this.map.iterator();
		while (true)
		{
			builder.append(iterator.next().getKey());
			if (iterator.hasNext())
			{
				builder.append(", ");
			}
			else
			{
				break;
			}
		}
		return builder.append("]").toString();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return Set.setEquals(this, obj);
	}
	
	@Override
	public int hashCode()
	{
		return Set.setHashCode(this);
	}
}
