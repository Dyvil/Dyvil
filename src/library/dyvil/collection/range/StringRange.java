package dyvil.collection.range;

import java.util.Iterator;
import java.util.function.Consumer;

import dyvil.lang.literal.TupleConvertible;

import dyvil.collection.Range;

@TupleConvertible
public class StringRange implements Range<String>
{
	protected final String	first;
	protected final String	last;
	
	public static StringRange apply(String first, String last)
	{
		return new StringRange(first, last);
	}
	
	public StringRange(String first, String last)
	{
		this.first = first;
		this.last = last;
	}
	
	public static int distance(String s1, String s2)
	{
		return Integer.parseInt(s2, 36) - Integer.parseInt(s1, 36);
	}
	
	public static String next(String s)
	{
		int len = s.length();
		
		StringBuilder builder = new StringBuilder(s);
		for (int i = len - 1; i >= 0; i--)
		{
			char c = s.charAt(i);
			if (c >= 'a' && c < 'z' || c >= 'A' && c < 'Z')
			{
				builder.setCharAt(i, (char) (c + 1));
				break;
			}
			if (c == 'z' || c == 'Z')
			{
				builder.setCharAt(i, (char) (c - 25));
				
				if (i == 0)
				{
					return null;
				}
			}
		}
		return builder.toString();
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
		return distance(this.first, this.last) + 1;
	}
	
	@Override
	public int estimateCount()
	{
		return -1;
	}
	
	@Override
	public Iterator<String> iterator()
	{
		return new Iterator<String>()
		{
			private String	current	= StringRange.this.first;
			
			@Override
			public boolean hasNext()
			{
				return this.current != null && this.current.compareTo(StringRange.this.last) <= 0;
			}
			
			@Override
			public String next()
			{
				String s = this.current;
				this.current = StringRange.next(s);
				return s;
			}
		};
	}
	
	@Override
	public void forEach(Consumer<? super String> action)
	{
		for (String current = this.first; current.compareTo(this.last) <= 0; current = next(current))
		{
			action.accept(current);
		}
	}
	
	@Override
	public boolean $qmark(Object o)
	{
		String s = (String) o;
		return s.compareTo(this.first) >= 0 && s.compareTo(this.last) <= 0;
	}
	
	@Override
	public void toArray(int index, Object[] store)
	{
		for (String current = this.first; current.compareTo(this.last) <= 0; current = next(current))
		{
			store[index++] = current;
		}
	}
	
	@Override
	public Range<String> copy()
	{
		return new StringRange(this.first, this.last);
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
