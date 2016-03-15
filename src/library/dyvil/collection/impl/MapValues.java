package dyvil.collection.impl;

import dyvil.collection.Collection;
import dyvil.collection.Map;
import dyvil.collection.Queryable;
import dyvil.collection.Set;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

public class MapValues<V> implements Queryable<V>
{
	private final Map<?, V> entries;

	public MapValues(Map<?, V> entries)
	{
		this.entries = entries;
	}

	@Override
	public int size()
	{
		return this.entries.size();
	}

	@Override
	public Iterator<V> iterator()
	{
		return this.entries.valueIterator();
	}

	@Override
	public void map(Function<? super V, ? extends V> mapper)
	{
		this.entries.mapValues(mapper);
	}

	@Override
	public void flatMap(Function<? super V, ? extends Iterable<? extends V>> mapper)
	{
		throw new UnsupportedOperationException("flatMap() on Map Values");
	}

	@Override
	public void filter(Predicate<? super V> condition)
	{
		this.entries.filterByValue(condition);
	}

	@Override
	public <R> Queryable<R> mapped(Function<? super V, ? extends R> mapper)
	{
		return this.entries.valueMapped(mapper).values();
	}

	@Override
	public <R> Queryable<R> flatMapped(Function<? super V, ? extends Iterable<? extends R>> mapper)
	{
		throw new UnsupportedOperationException("flatMapped() on Map Values");
	}

	@Override
	public Queryable<V> filtered(Predicate<? super V> condition)
	{
		return this.entries.filteredByValue(condition).values();
	}

	@Override
	public String toString()
	{
		return this.toString(Set.START_STRING, Set.ELEMENT_SEPARATOR_STRING, Set.END_STRING);
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof MapValues && Collection.unorderedEquals(this, (MapValues<V>) obj);
	}

	@Override
	public int hashCode()
	{
		return Collection.unorderedHashCode(this);
	}
}
