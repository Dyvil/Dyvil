package dyvil.collection.impl;

import dyvil.collection.Collection;
import dyvil.collection.Map;
import dyvil.collection.Queryable;
import dyvil.collection.Set;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

public class MapKeys<K> implements Queryable<K>
{
	private final Map<K, ?> entries;

	public MapKeys(Map<K, ?> entries)
	{
		this.entries = entries;
	}

	@Override
	public int size()
	{
		return this.entries.size();
	}

	@Override
	public Iterator<K> iterator()
	{
		return this.entries.keyIterator();
	}

	@Override
	public void map(Function<? super K, ? extends K> mapper)
	{
		this.entries.mapKeys(mapper);
	}

	@Override
	public void flatMap(Function<? super K, ? extends Iterable<? extends K>> mapper)
	{
		throw new UnsupportedOperationException("flatMap() on Map Keys");
	}

	@Override
	public void filter(Predicate<? super K> condition)
	{
		this.entries.filterByKey(condition);
	}

	@Override
	public <R> Queryable<R> mapped(Function<? super K, ? extends R> mapper)
	{
		return this.entries.keyMapped(mapper).keys();
	}

	@Override
	public <R> Queryable<R> flatMapped(Function<? super K, ? extends Iterable<? extends R>> mapper)
	{
		throw new UnsupportedOperationException("flatMapped() on Map Keys");
	}

	@Override
	public Queryable<K> filtered(Predicate<? super K> condition)
	{
		return this.entries.filteredByKey(condition).keys();
	}

	@Override
	public String toString()
	{
		return this.toString(Set.START_STRING, Set.ELEMENT_SEPARATOR_STRING, Set.END_STRING);
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof MapKeys && Collection.unorderedEquals(this, (MapKeys<K>) obj);
	}

	@Override
	public int hashCode()
	{
		return Collection.unorderedHashCode(this);
	}
}
