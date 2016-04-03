package dyvil.collection.mutable;

import dyvil.collection.*;
import dyvil.collection.impl.AbstractMapBasedSet;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

public class MapBasedSet<E> extends AbstractMapBasedSet<E> implements MutableSet<E>
{
	private static final long serialVersionUID = 3329100687699880194L;

	protected MutableMap<E, Boolean> map;

	public MapBasedSet(MutableMap<E, Boolean> map)
	{
		this.map = map;
	}

	@Override
	protected Map<E, Boolean> map()
	{
		return this.map;
	}

	@Override
	public MutableSet<E> added(E element)
	{
		return new MapBasedSet<>(this.map.withEntry(element, true));
	}

	@Override
	public MutableSet<E> removed(Object element)
	{
		return new MapBasedSet<>(this.map.keyRemoved(element));
	}

	@Override
	public MutableSet<? extends E> difference(Collection<?> collection)
	{
		return new MapBasedSet<>(this.map.keyDifference(collection));
	}

	@Override
	public MutableSet<? extends E> intersection(Collection<? extends E> collection)
	{
		MutableMap<E, Boolean> map = this.map.emptyCopy();
		for (Entry<E, ?> entry : this.map)
		{
			E element = entry.getKey();
			if (collection.contains(element))
			{
				map.subscript_$eq(element, true);
			}
		}
		return new MapBasedSet<>(map);
	}

	@Override
	public MutableSet<? extends E> union(Collection<? extends E> collection)
	{
		MutableMap<E, Boolean> map = this.map.copy();
		for (E element : collection)
		{
			map.subscript_$eq(element, true);
		}
		return new MapBasedSet<>(map);
	}

	@Override
	public MutableSet<? extends E> symmetricDifference(Collection<? extends E> collection)
	{
		MutableMap<E, Boolean> map = this.map.emptyCopy();
		for (Entry<E, ?> entry : this.map)
		{
			E element = entry.getKey();
			if (!collection.contains(element))
			{
				map.subscript_$eq(element, true);
			}
		}
		for (E element : collection)
		{
			if (!this.contains(element))
			{
				map.subscript_$eq(element, true);
			}
		}
		return new MapBasedSet<>(map);
	}

	@Override
	public <R> MutableSet<R> mapped(Function<? super E, ? extends R> mapper)
	{
		MutableMap<R, Boolean> map = this.map.emptyCopy();
		for (Entry<E, ?> entry : this.map)
		{
			map.subscript_$eq(mapper.apply(entry.getKey()), true);
		}
		return new MapBasedSet<>(map);
	}

	@Override
	public <R> MutableSet<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		MutableMap<R, Boolean> map = this.map.emptyCopy();
		for (Entry<E, ?> entry : this.map)
		{
			for (R element : mapper.apply(entry.getKey()))
			{
				map.subscript_$eq(element, true);
			}
		}
		return new MapBasedSet<>(map);
	}

	@Override
	public MutableSet<E> filtered(Predicate<? super E> condition)
	{
		MutableMap<E, Boolean> map = this.map.emptyCopy();
		for (Entry<E, ?> entry : this.map)
		{
			E element = entry.getKey();
			if (condition.test(element))
			{
				map.subscript_$eq(element, true);
			}
		}
		return new MapBasedSet<>(map);
	}

	@Override
	public void clear()
	{
		this.map.clear();
	}

	@SuppressWarnings("PointlessBooleanExpression")
	@Override
	public boolean add(E element)
	{
		return this.map.put(element, true) == null;
	}

	@Override
	public boolean remove(Object element)
	{
		return this.map.removeKey(element);
	}

	@Override
	public boolean addAll(Collection<? extends E> collection)
	{
		boolean added = false;
		for (E element : collection)
		{
			if (this.map.put(element, true) == Boolean.TRUE)
			{
				added = true;
			}
		}
		return added;
	}

	@Override
	public boolean retainAll(Collection<? extends E> collection)
	{
		boolean removed = false;
		Iterator<? extends Entry<E, ?>> iterator = this.map.iterator();
		while (iterator.hasNext())
		{
			E element = iterator.next().getKey();
			if (!collection.contains(element))
			{
				iterator.remove();
				removed = true;
			}
		}
		return removed;
	}

	@Override
	public boolean symmetricDifferenceInplace(Collection<? extends E> collection)
	{
		boolean changed = false;
		MutableMap<E, Boolean> newMap = this.map.emptyCopy();
		for (Entry<E, ?> entry : this.map)
		{
			E element = entry.getKey();
			if (!collection.contains(element))
			{
				newMap.subscript_$eq(element, true);
				changed = true;
			}
		}
		for (E element : collection)
		{
			if (!this.contains(element))
			{
				newMap.subscript_$eq(element, true);
				changed = true;
			}
		}
		this.map = newMap;
		return changed;
	}

	@Override
	public void map(Function<? super E, ? extends E> mapper)
	{
		MutableMap<E, Boolean> newMap = this.map.emptyCopy();
		for (Entry<E, ?> entry : this.map)
		{
			newMap.subscript_$eq(mapper.apply(entry.getKey()), true);
		}
		this.map = newMap;
	}

	@Override
	public void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper)
	{
		MutableMap<E, Boolean> newMap = this.map.emptyCopy();
		for (Entry<E, ?> entry : this.map)
		{
			for (E element : mapper.apply(entry.getKey()))
			{
				newMap.subscript_$eq(element, true);
			}
		}
		this.map = newMap;
	}

	@Override
	public MutableSet<E> copy()
	{
		return this.mutableCopy();
	}

	@Override
	public ImmutableSet<E> immutable()
	{
		return this.immutableCopy();
	}
}
