package dyvil.lang.ref.simple;

import dyvil.lang.literal.LongConvertible;
import dyvil.lang.ref.LongRef;

@LongConvertible
public class SimpleLongRef implements LongRef
{
	public long value;
	
	public static SimpleLongRef apply(long value)
	{
		return new SimpleLongRef(value);
	}
	
	public SimpleLongRef(long value)
	{
		this.value = value;
	}
	
	@Override
	public long get()
	{
		return this.value;
	}
	
	@Override
	public void set(long value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return Long.toString(this.value);
	}
}
