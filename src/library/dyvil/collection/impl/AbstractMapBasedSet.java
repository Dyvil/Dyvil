package dyvil.collection.impl;

import dyvil.collection.Entry;
import dyvil.collection.Map;
import dyvil.collection.Set;
import java.lang.Boolean;

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
		for (Entry<E, ?> e : this.map())
		{
			store[index++] = e.getKey();
		}
	}
	
	@Override
	public java.util.Set<E> toJava()
	{
		java.util.Map<E, Boolean> map = this.map().toJava();
		return Collections.newSetFromMap(map);
	}
	
	@Override
	public String toString()
	{
		if (this.isEmpty())
		{
			return "[]";
		}
		
		StringBuilder builder = new StringBuilder("[");
		Iterator<? extends Entry<E, ?>> iterator = this.map().iterator();
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
