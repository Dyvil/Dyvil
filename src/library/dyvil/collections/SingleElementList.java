package dyvil.collections;

import java.lang.reflect.Array;
import java.util.*;

public class SingleElementList<E> implements List<E>
{
	private final class Itr implements Iterator<E>, ListIterator<E>
	{
		protected boolean	returned;
		
		@Override
		public boolean hasNext()
		{
			return !this.returned;
		}
		
		@Override
		public E next()
		{
			if (this.returned)
			{
				throw new NoSuchElementException();
			}
			return element;
		}
		
		@Override
		public boolean hasPrevious()
		{
			return false;
		}
		
		@Override
		public E previous()
		{
			return null;
		}
		
		@Override
		public int nextIndex()
		{
			return 0;
		}
		
		@Override
		public int previousIndex()
		{
			return 0;
		}
		
		@Override
		public void remove()
		{
		}
		
		@Override
		public void set(E e)
		{
			if (this.returned)
			{
				element = e;
			}
		}
		
		@Override
		public void add(E e)
		{
			if (!this.returned)
			{
				element = e;
			}
		}
	}
	
	protected E	element;
	
	public SingleElementList()
	{
	}
	
	public SingleElementList(E element)
	{
		this.element = element;
	}
	
	@Override
	public int size()
	{
		return 1;
	}
	
	@Override
	public boolean isEmpty()
	{
		return false;
	}
	
	@Override
	public boolean contains(Object o)
	{
		return Objects.equals(o, this.element);
	}
	
	@Override
	public Iterator<E> iterator()
	{
		return new Itr();
	}
	
	@Override
	public Object[] toArray()
	{
		return new Object[] { this.element };
	}
	
	@Override
	public <T> T[] toArray(T[] a)
	{
		if (a.length < 1)
		{
			a = (T[]) Array.newInstance(a.getClass().getComponentType(), 1);
		}
		a[0] = (T) this.element;
		return a;
	}
	
	@Override
	public boolean add(E e)
	{
		this.element = e;
		return true;
	}
	
	@Override
	public boolean remove(Object o)
	{
		return false;
	}
	
	@Override
	public boolean containsAll(Collection<?> c)
	{
		return false;
	}
	
	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		return false;
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends E> c)
	{
		return false;
	}
	
	@Override
	public boolean removeAll(Collection<?> c)
	{
		return false;
	}
	
	@Override
	public boolean retainAll(Collection<?> c)
	{
		return false;
	}
	
	@Override
	public void clear()
	{
	}
	
	@Override
	public E get(int index)
	{
		if (index != 0)
		{
			throw new ArrayIndexOutOfBoundsException();
		}
		return this.element;
	}
	
	@Override
	public E set(int index, E element)
	{
		if (index != 0)
		{
			throw new ArrayIndexOutOfBoundsException();
		}
		E e = this.element;
		this.element = element;
		return e;
	}
	
	@Override
	public void add(int index, E element)
	{
	}
	
	@Override
	public E remove(int index)
	{
		return null;
	}
	
	@Override
	public int indexOf(Object o)
	{
		return Objects.equals(o, this.element) ? 0 : -1;
	}
	
	@Override
	public int lastIndexOf(Object o)
	{
		return Objects.equals(o, this.element) ? 0 : -1;
	}
	
	@Override
	public ListIterator<E> listIterator()
	{
		return new Itr();
	}
	
	@Override
	public ListIterator<E> listIterator(int index)
	{
		if (index != 0)
		{
			throw new ArrayIndexOutOfBoundsException();
		}
		return new Itr();
	}
	
	@Override
	public List<E> subList(int fromIndex, int toIndex)
	{
		if (fromIndex != 0 || toIndex != 0)
		{
			throw new ArrayIndexOutOfBoundsException();
		}
		return this;
	}
	
}
