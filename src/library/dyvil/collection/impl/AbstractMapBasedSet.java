package dyvil.collection.impl;

import java.util.Iterator;

import dyvil.collection.Entry;
import dyvil.collection.Map;
import dyvil.collection.Set;

public abstract class AbstractMapBasedSet<E> implements Set<E>
{
	private static final long serialVersionUID = -6579037312574546078L;

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
		return new java.util.AbstractSet<E>()
		{		
			@Override
			public int size()
			{
				return AbstractMapBasedSet.this.map().size();
			}

			@Override
			public Iterator<E> iterator()
			{
				return AbstractMapBasedSet.this.map().keyIterator();
			}
			
			@Override
			public boolean contains(Object o)
			{
				return AbstractMapBasedSet.this.map().containsKey(o);
			}
			
			@Override
			public void clear()
			{
				AbstractMapBasedSet.this.map().clear();
			}
			
			@Override
			public boolean add(E e)
			{
				return AbstractMapBasedSet.this.map().put(e, VALUE) == null;
			}
			
			@Override
			public boolean remove(Object o)
			{
				return AbstractMapBasedSet.this.map().removeKey(o) != null;
			}
		};
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
