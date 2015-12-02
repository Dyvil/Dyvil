package dyvil.collection.mutable;

import dyvil.collection.*;
import dyvil.collection.impl.AbstractMapBasedSet;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

public class MapBasedSet<E> extends AbstractMapBasedSet<E> implements MutableSet<E>
{
	private static final long serialVersionUID = 3329100687699880194L;
	
	protected MutableMap<E, Object> map;
	
	public MapBasedSet(MutableMap<E, ? extends Object> map)
	{
		this.map = (MutableMap<E, Object>) map;
	}
	
	@Override
	protected Map<E, Object> map()
	{
		return this.map;
	}
	
	@Override
	public MutableSet<E> $plus(E element)
	{
		return new MapBasedSet(this.map.$plus(element, VALUE));
	}
	
	@Override
	public MutableSet<E> $minus(Object element)
	{
		return new MapBasedSet(this.map.$minus$at(element));
	}
	
	@Override
	public MutableSet<? extends E> $minus$minus(Collection<?> collection)
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
		return new MapBasedSet(map);
	}
	
	@Override
	public MutableSet<? extends E> $amp(Collection<? extends E> collection)
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
		return new MapBasedSet(map);
	}
	
	@Override
	public MutableSet<? extends E> $bar(Collection<? extends E> collection)
	{
		MutableMap<E, Object> map = this.map.copy();
		for (E element : collection)
		{
			map.subscript_$eq(element, VALUE);
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
		return new MapBasedSet(map);
	}
	
	@Override
	public <R> MutableSet<R> mapped(Function<? super E, ? extends R> mapper)
	{
		MutableMap<R, Object> map = new HashMap();
		for (Entry<E, Object> entry : this.map)
		{
			map.subscript_$eq(mapper.apply(entry.getKey()), VALUE);
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
				map.subscript_$eq(element, VALUE);
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
				map.subscript_$eq(element, VALUE);
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
		return this.map.put(element, VALUE) == null;
	}
	
	@Override
	public boolean remove(Object element)
	{
		return this.map.removeKey(element) == VALUE;
	}
	
	@Override
	public void $amp$eq(Collection<? extends E> collection)
	{
		Iterator<Entry<E, Object>> iterator = this.map.iterator();
		while (iterator.hasNext())
		{
			E element = iterator.next().getKey();
			if (!collection.contains(element))
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
			this.map.subscript_$eq(element, VALUE);
		}
	}
	
	@Override
	public void $up$eq(Collection<? extends E> collection)
	{
		HashMap<E, Object> newMap = new HashMap();
		for (Entry<E, Object> entry : this.map)
		{
			E element = entry.getKey();
			if (!collection.contains(element))
			{
				newMap.subscript_$eq(element, VALUE);
			}
		}
		for (E element : collection)
		{
			if (!this.contains(element))
			{
				newMap.subscript_$eq(element, VALUE);
			}
		}
		this.map = newMap;
	}
	
	@Override
	public void map(Function<? super E, ? extends E> mapper)
	{
		HashMap<E, Object> newMap = new HashMap();
		for (Entry<E, Object> entry : this.map)
		{
			newMap.subscript_$eq(mapper.apply(entry.getKey()), VALUE);
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
				newMap.subscript_$eq(element, VALUE);
			}
		}
		this.map = newMap;
	}
	
	@Override
	public MutableSet<E> copy()
	{
		return new MapBasedSet(this.map.copy());
	}
	
	@Override
	public MutableSet<E> emptyCopy()
	{
		return new MapBasedSet(this.map.emptyCopy());
	}
	
	@Override
	public ImmutableSet<E> immutable()
	{
		return new dyvil.collection.immutable.MapBasedSet<E>(this.map.immutable());
	}
}
