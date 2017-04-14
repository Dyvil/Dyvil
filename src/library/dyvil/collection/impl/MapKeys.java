package dyvil.collection.impl;

import dyvil.annotation.internal.NonNull;
import dyvil.collection.*;

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

	@NonNull
	@Override
	public Iterator<K> iterator()
	{
		return this.entries.keyIterator();
	}

	@Override
	public void map(@NonNull Function<? super K, ? extends K> mapper)
	{
		this.entries.mapKeys(mapper);
	}

	@Override
	public void flatMap(@NonNull Function<? super K, ? extends @NonNull Iterable<? extends K>> mapper)
	{
		throw new UnsupportedOperationException("flatMap() on Map Keys");
	}

	@Override
	public void filter(@NonNull Predicate<? super K> predicate)
	{
		this.entries.filterByKey(predicate);
	}

	@NonNull
	@Override
	public <R> Queryable<R> mapped(@NonNull Function<? super K, ? extends R> mapper)
	{
		final Map<R, ?> entries = this.entries.keyMapped(mapper);
		return entries.keys();
	}

	@NonNull
	@Override
	public <R> Queryable<R> flatMapped(@NonNull Function<? super K, ? extends @NonNull Iterable<? extends R>> mapper)
	{
		throw new UnsupportedOperationException("flatMapped() on Map Keys");
	}

	@NonNull
	@Override
	public Queryable<K> filtered(@NonNull Predicate<? super K> predicate)
	{
		return this.entries.filteredByKey(predicate).keys();
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
		return obj instanceof MapKeys && Collection.unorderedEquals(this, (MapKeys<K>) obj);
	}

	@Override
	public int hashCode()
	{
		return Collection.unorderedHashCode(this);
	}
}
