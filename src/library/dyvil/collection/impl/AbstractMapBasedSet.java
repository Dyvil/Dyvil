package dyvil.collection.impl;

import java.util.Collections;
import java.util.Iterator;

import dyvil.collection.Entry;
import dyvil.collection.Map;
import dyvil.collection.Set;

public abstract class AbstractMapBasedSet<E> implements Set<E>
{
	protected abstract Map<E, Object> map();
	
	@Override
	public int size()
	{
		return this.map().size();
	}
	
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
	public void toArray(int index, Object[] store)
	{
		for (Entry<E, Object> e : this.map())
		{
			store[index++] = e.getKey();
		}
	}
	
	@Override
	public java.util.Set<E> toJava()
	{
		// TODO Ensure type safety
		return Collections.newSetFromMap((java.util.Map) this.map().toJava());
	}
	
	@Override
	public String toString()
	{
		if (this.isEmpty())
		{
			return "[]";
		}
		
		StringBuilder builder = new StringBuilder("[");
		Iterator<Entry<E, Object>> iterator = this.map().iterator();
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
