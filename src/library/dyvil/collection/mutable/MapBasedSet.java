package dyvil.collection.mutable;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableMap;
import dyvil.collection.MutableSet;
import dyvil.lang.Collection;
import dyvil.lang.Entry;
import dyvil.lang.Set;

public class MapBasedSet<E> implements MutableSet<E>
{
	private static final Object			VALUE	= new Object();
	private final MutableMap<E, Object>	map;
	
	public MapBasedSet(MutableMap<E, ? extends Object> map)
	{
		this.map = (MutableMap<E, Object>) map;
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
	public MutableSet<E> $plus(E element)
	{
		return new MapBasedSet(this.map.$plus(element, VALUE));
	}
	
	@Override
	public MutableSet<E> $minus(Object element)
	{
		return new MapBasedSet(this.map.$minus(element));
	}
	
	@Override
	public MutableSet<? extends E> $minus$minus(Collection<? extends E> collection)
	{
		return null; // TODO MapBasedSet --
	}
	
	@Override
	public MutableSet<? extends E> $amp(Collection<? extends E> collection)
	{
		return null; // TODO MapBasedSet &
	}
	
	@Override
	public MutableSet<? extends E> $bar(Collection<? extends E> collection)
	{
		return null; // TODO MapBasedSet |
	}
	
	@Override
	public MutableSet<? extends E> $up(Collection<? extends E> collection)
	{
		return null; // TODO MapBasedSet ^
	}
	
	@Override
	public <R> MutableSet<R> mapped(Function<? super E, ? extends R> mapper)
	{
		return null; // TODO MapBasedSet.mapped
	}
	
	@Override
	public <R> MutableSet<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		return null; // TODO MapBasedSet.flatMapped
	}
	
	@Override
	public MutableSet<E> filtered(Predicate<? super E> condition)
	{
		return null; // TODO MapBasedSet.filtered
	}
	
	@Override
	public boolean add(E element)
	{
		return this.map.put(element, VALUE) != null;
	}
	
	@Override
	public boolean remove(E element)
	{
		return this.map.remove(element) == VALUE;
	}
	
	@Override
	public void $amp$eq(Collection<? extends E> collection)
	{
		// TODO MapBasedSet &=
	}
	
	@Override
	public void $bar$eq(Collection<? extends E> collection)
	{
		for (E element : collection)
		{
			this.map.update(element, VALUE);
		}
	}
	
	@Override
	public void $up$eq(Collection<? extends E> collection)
	{
		// TODO MapBasedSet ^=
	}
	
	@Override
	public void clear()
	{
		this.map.clear();
	}
	
	@Override
	public void map(UnaryOperator<E> mapper)
	{
		// TODO MapBasedSet.map
	}
	
	@Override
	public void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper)
	{
		// TODO MapBasedSet.flatMap
	}
	
	@Override
	public void filter(Predicate<? super E> condition)
	{
		// TODO MapBasedSet.filter
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
	public MutableSet<E> copy()
	{
		return new MapBasedSet(this.map.copy());
	}
	
	@Override
	public ImmutableSet<E> immutable()
	{
		// TODO Add the immutable.MapBasedSet implementation when this one is ready
		return null;
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
