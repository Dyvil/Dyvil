package dyvil.collection.impl;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.*;

import java.util.Collections;
import java.util.Iterator;

public abstract class AbstractMapBasedSet<E> implements Set<E>
{
	private static final long serialVersionUID = -6579037312574546078L;

	protected abstract Map<E, Boolean> map();

	@Override
	public int size()
	{
		return this.map().size();
	}

	@NonNull
	@Override
	public Iterator<E> iterator()
	{
		return this.map().keyIterator();
	}

	@Override
	public boolean contains(Object element)
	{
		return this.map().containsKey(element);
	}

	@Override
	public void toArray(int index, Object @NonNull [] store)
	{
		for (Entry<E, ?> e : this.map())
		{
			store[index++] = e.getKey();
		}
	}

	@NonNull
	@Override
	public <R> MutableSet<R> emptyCopy()
	{
		return new dyvil.collection.mutable.MapBasedSet<>(this.map().emptyCopy());
	}

	@NonNull
	@Override
	public <RE> MutableSet<RE> emptyCopy(int capacity)
	{
		return null;
	}

	@NonNull
	@Override
	public MutableSet<E> mutableCopy()
	{
		return new dyvil.collection.mutable.MapBasedSet<>(this.map().mutableCopy());
	}

	@NonNull
	@Override
	public ImmutableSet<E> immutableCopy()
	{
		return new dyvil.collection.immutable.MapBasedSet<>(this.map().immutableCopy());
	}

	@Override
	public <RE> ImmutableSet.Builder<RE> immutableBuilder()
	{
		return dyvil.collection.immutable.MapBasedSet.builder(this.map().immutableBuilder());
	}

	@Override
	public <RE> ImmutableSet.Builder<RE> immutableBuilder(int capacity)
	{
		return dyvil.collection.immutable.MapBasedSet.builder(this.map().immutableBuilder(capacity));
	}

	@Override
	public java.util.Set<E> toJava()
	{
		java.util.Map<E, Boolean> map = this.map().toJava();
		return Collections.newSetFromMap(map);
	}

	@NonNull
	@Override
	public String toString()
	{
		if (this.isEmpty())
		{
			return Collection.EMPTY_STRING;
		}

		final StringBuilder builder = new StringBuilder(this.size() << 3).append(Collection.START_STRING);

		for (Entry<E, ?> entry : this.map())
		{
			builder.append(entry.getKey()).append(Collection.ELEMENT_SEPARATOR_STRING);
		}

		final int len = builder.length();
		return builder.replace(len - Collection.ELEMENT_SEPARATOR_STRING.length(), len, Collection.END_STRING)
		              .toString();
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
