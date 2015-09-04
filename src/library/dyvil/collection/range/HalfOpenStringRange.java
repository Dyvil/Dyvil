package dyvil.collection.range;

import java.util.Iterator;
import java.util.function.Consumer;

import dyvil.lang.literal.TupleConvertible;

import dyvil.collection.Range;
import dyvil.string.StringUtils;

@TupleConvertible
public class HalfOpenStringRange implements Range<String>
{
	protected final String	first;
	protected final String	last;
	
	public static HalfOpenStringRange apply(String first, String last)
	{
		return new HalfOpenStringRange(first, last);
	}
	
	public HalfOpenStringRange(String first, String last)
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
		return StringUtils.alphaDistance(this.first, this.last);
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
	public Iterator<String> iterator()
	{
		return new Iterator<String>()
		{
			private String current = HalfOpenStringRange.this.first;
			
			@Override
			public boolean hasNext()
			{
				return this.current != null && StringUtils.compareAlpha(this.current, HalfOpenStringRange.this.last) < 0;
			}
			
			@Override
			public String next()
			{
				String s = this.current;
				this.current = StringUtils.nextAlpha(s);
				return s;
			}
		};
	}
	
	@Override
	public void forEach(Consumer<? super String> action)
	{
		for (String current = this.first; StringUtils.compareAlpha(current, this.last) < 0; current = StringUtils.nextAlpha(current))
		{
			action.accept(current);
		}
	}
	
	@Override
	public boolean contains(Object o)
	{
		String s = (String) o;
		return s.compareTo(this.first) >= 0 && s.compareTo(this.last) < 0;
	}
	
	@Override
	public void toArray(int index, Object[] store)
	{
		for (String current = this.first; StringUtils.compareAlpha(current, this.last) < 0; current = StringUtils.nextAlpha(current))
		{
			store[index++] = current;
		}
	}
	
	@Override
	public Range<String> copy()
	{
		return new HalfOpenStringRange(this.first, this.last);
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
}
