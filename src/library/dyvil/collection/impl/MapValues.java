package dyvil.collection.impl;

import dyvil.annotation.internal.NonNull;
import dyvil.collection.*;

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

	@NonNull
	@Override
	public Iterator<V> iterator()
	{
		return this.entries.valueIterator();
	}

	@Override
	public void map(@NonNull Function<? super V, ? extends V> mapper)
	{
		this.entries.mapValues(mapper);
	}

	@Override
	public void flatMap(@NonNull Function<? super V, ? extends @NonNull Iterable<? extends V>> mapper)
	{
		throw new UnsupportedOperationException("flatMap() on Map Values");
	}

	@Override
	public void filter(@NonNull Predicate<? super V> condition)
	{
		this.entries.filterByValue(condition);
	}

	@NonNull
	@Override
	public <R> Queryable<R> mapped(@NonNull Function<? super V, ? extends R> mapper)
	{
		final Map<?, R> entries = this.entries.valueMapped(mapper);
		return entries.values();
	}

	@NonNull
	@Override
	public <R> Queryable<R> flatMapped(@NonNull Function<? super V, ? extends @NonNull Iterable<? extends R>> mapper)
	{
		throw new UnsupportedOperationException("flatMapped() on Map Values");
	}

	@NonNull
	@Override
	public Queryable<V> filtered(@NonNull Predicate<? super V> condition)
	{
		return this.entries.filteredByValue(condition).values();
	}

	@NonNull
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
