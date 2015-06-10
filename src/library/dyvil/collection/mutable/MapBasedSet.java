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
	protected MutableMap<E, Object>	map;
	
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
		MutableMap<E, Object> map = new HashMap();
		for (Entry<E, Object> entry : this.map)
		{
			E element = entry.getKey();
			if (!collection.$qmark(element))
			{
				map.update(element, VALUE);
			}
		}
		return new MapBasedSet(map);
	}
	
	@Override
	public MutableSet<? extends E> $amp(Collection<? extends E> collection)
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
		return new MapBasedSet(map);
	}
	
	@Override
	public MutableSet<? extends E> $bar(Collection<? extends E> collection)
	{
		MutableMap<E, Object> map = this.map.copy();
		for (E element : collection)
		{
			map.update(element, VALUE);
		}
		return new MapBasedSet(map);
	}
	
	@Override
	public MutableSet<? extends E> $up(Collection<? extends E> collection)
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
		return new MapBasedSet(map);
	}
	
	@Override
	public <R> MutableSet<R> mapped(Function<? super E, ? extends R> mapper)
	{
		MutableMap<R, Object> map = new HashMap();
		for (Entry<E, Object> entry : this.map)
		{
			map.update(mapper.apply(entry.getKey()), VALUE);
		}
		return new MapBasedSet(map);
	}
	
	@Override
	public <R> MutableSet<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		MutableMap<R, Object> map = new HashMap();
		for (Entry<E, Object> entry : this.map)
		{
			for (R element : mapper.apply(entry.getKey()))
			{
				map.update(element, VALUE);
			}
		}
		return new MapBasedSet(map);
	}
	
	@Override
	public MutableSet<E> filtered(Predicate<? super E> condition)
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
		return new MapBasedSet(map);
	}
	
	@Override
	public void clear()
	{
		this.map.clear();
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
		Iterator<Entry<E, Object>> iterator = this.map.iterator();
		while (iterator.hasNext())
		{
			E element = iterator.next().getKey();
			if (!collection.$qmark(element))
			{
				iterator.remove();
			}
		}
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
		HashMap<E, Object> newMap = new HashMap();
		for (Entry<E, Object> entry : this.map)
		{
			E element = entry.getKey();
			if (!collection.$qmark(element))
			{
				newMap.update(element, VALUE);
			}
		}
		for (E element : collection)
		{
			if (!this.$qmark(element))
			{
				newMap.update(element, VALUE);
			}
		}
		this.map = newMap;
	}
	
	@Override
	public void map(UnaryOperator<E> mapper)
	{
		HashMap<E, Object> newMap = new HashMap();
		for (Entry<E, Object> entry : this.map)
		{
			newMap.update(mapper.apply(entry.getKey()), VALUE);
		}
		this.map = newMap;
	}
	
	@Override
	public void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper)
	{
		HashMap<E, Object> newMap = new HashMap();
		for (Entry<E, Object> entry : this.map)
		{
			for (E element : mapper.apply(entry.getKey()))
			{
				newMap.update(element, VALUE);
			}
		}
		this.map = newMap;
	}
	
	@Override
	public void filter(Predicate<? super E> condition)
	{
		Iterator<Entry<E, Object>> iterator = this.map.iterator();
		while (iterator.hasNext())
		{
			if (!condition.test(iterator.next().getKey()))
			{
				iterator.remove();
			}
		}
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
		// TODO immutable.MapBasedSet
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
