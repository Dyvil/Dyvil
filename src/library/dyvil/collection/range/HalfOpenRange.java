package dyvil.collection.range;

import dyvil.annotation.Immutable;
import dyvil.collection.Range;
import dyvil.lang.LiteralConvertible;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

@LiteralConvertible.FromTuple
@Immutable
public class HalfOpenRange<T extends Rangeable<T>> implements Range<T>
{
	private static final long serialVersionUID = -8656379367421762895L;
	
	protected transient T first;
	protected transient T last;
	
	public static <T extends Rangeable<T>> HalfOpenRange<T> apply(T first, T last)
	{
		return new HalfOpenRange<>(first, last);
	}
	
	public HalfOpenRange(T first, T last)
	{
		this.first = first;
		this.last = last;
	}
	
	@Override
	public T first()
	{
		return this.first;
	}
	
	@Override
	public T last()
	{
		return this.last.previous();
	}
	
	@Override
	public int count()
	{
		int count = 0;
		for (T current = this.first; current.compareTo(this.last) < 0; current = current.next())
		{
			count++;
		}
		return count;
	}
	
	@Override
	public int estimateCount()
	{
		return -1;
	}
	
	@Override
	public boolean isHalfOpen()
	{
		return true;
	}
	
	@Override
	public Iterator<T> iterator()
	{
		return new Iterator<T>()
		{
			private T current = HalfOpenRange.this.first;
			
			@Override
			public T next()
			{
				if (this.current.compareTo(HalfOpenRange.this.last) >= 0)
				{
					throw new NoSuchElementException("End of Range");
				}
				
				final T c = this.current;
				this.current = this.current.next();
				return c;
			}
			
			@Override
			public boolean hasNext()
			{
				return this.current.compareTo(HalfOpenRange.this.last) < 0;
			}
			
			@Override
			public String toString()
			{
				return "RangeIterator(" + HalfOpenRange.this + ")";
			}
		};
	}
	
	@Override
	public void forEach(Consumer<? super T> action)
	{
		for (T current = this.first; current.compareTo(this.last) < 0; current = current.next())
		{
			action.accept(current);
		}
	}
	
	@Override
	public void toArray(int index, Object[] store)
	{
		for (T current = this.first; current.compareTo(this.last) < 0; current = current.next())
		{
			store[index++] = current;
		}
	}
	
	@Override
	public boolean contains(Object o)
	{
		for (Rangeable<T> current = this.first; current.compareTo(this.last) < 0; current = current.next())
		{
			if (current.compareTo((T) o) == 0)
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Range<T> copy()
	{
		return new HalfOpenRange<>(this.first, this.last);
	}
	
	@Override
	public String toString()
	{
		return this.first + " ..< " + this.last;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return Range.rangeEquals(this, obj);
	}
	
	@Override
	public int hashCode()
	{
		return Range.rangeHashCode(this);
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
		
		out.writeObject(this.first);
		out.writeObject(this.last);
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		
		this.first = (T) in.readObject();
		this.last = (T) in.readObject();
	}
}
