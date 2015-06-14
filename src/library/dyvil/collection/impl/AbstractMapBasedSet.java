package dyvil.collection.impl;

import java.util.Iterator;

import dyvil.lang.Entry;
import dyvil.lang.Map;
import dyvil.lang.Set;

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
	public boolean $qmark(Object element)
	{
		return this.map().$qmark(element);
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
