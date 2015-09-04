package dyvil.collection.range;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import dyvil.lang.literal.TupleConvertible;

import dyvil.collection.Range;
import dyvil.string.StringUtils;

@TupleConvertible
public class ClosedStringRange implements Range<String>
{
	protected final String	first;
	protected final String	last;
	
	public static ClosedStringRange apply(String first, String last)
	{
		return new ClosedStringRange(first, last);
	}
	
	public ClosedStringRange(String first, String last)
	{
		this.first = first;
		this.last = last;
	}
	
	@Override
	public String first()
	{
		return this.first;
	}
	
	@Override
	public String last()
	{
		return this.last;
	}
	
	@Override
	public int count()
	{
		return StringUtils.alphaDistance(this.first, this.last) + 1;
	}
	
	@Override
	public int estimateCount()
	{
		return -1;
	}
	
	@Override
	public boolean isHalfOpen()
	{
		return false;
	}
	
	@Override
	public Iterator<String> iterator()
	{
		return new Iterator<String>()
		{
			private String current = ClosedStringRange.this.first;
			
			@Override
			public boolean hasNext()
			{
				return this.current != null && StringUtils.compareAlpha(this.current, ClosedStringRange.this.last) <= 0;
			}
			
			@Override
			public String next()
			{
				String s = this.current;
				if (s == null)
				{
					throw new NoSuchElementException();
				}
				
				this.current = StringUtils.nextAlpha(s);
				
				if (this.current == s)
				{
					this.current = null;
				}
				return s;
			}
		};
	}
	
	@Override
	public void forEach(Consumer<? super String> action)
	{
		for (String current = this.first; StringUtils.compareAlpha(current, this.last) <= 0; current = StringUtils.nextAlpha(current))
		{
			action.accept(current);
		}
	}
	
	@Override
	public boolean contains(Object o)
	{
		if (!(o instanceof String))
		{
			return false;
		}
		
		String s = (String) o;
		return StringUtils.compareAlpha(s, this.last) >= 0 && StringUtils.compareAlpha(s, this.last) <= 0;
	}
	
	@Override
	public void toArray(int index, Object[] store)
	{
		for (String current = this.first; StringUtils.compareAlpha(current, this.last) <= 0; current = StringUtils.nextAlpha(current))
		{
			store[index++] = current;
		}
	}
	
	@Override
	public Range<String> copy()
	{
		return new ClosedStringRange(this.first, this.last);
	}
	
	@Override
	public String toString()
	{
		return this.first + " .. " + this.last;
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
}
