package dyvil.lang.ref.simple;

import dyvil.lang.literal.LongConvertible;
import dyvil.lang.ref.ILongRef;

@LongConvertible
public class LongRef implements ILongRef
{
	public long	value;
	
	public static LongRef apply(long value)
	{
		return new LongRef(value);
	}
	
	public LongRef(long value)
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
