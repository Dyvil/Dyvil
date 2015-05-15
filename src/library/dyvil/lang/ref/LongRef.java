package dyvil.lang.ref;

import dyvil.lang.literal.LongConvertible;

@LongConvertible
public class LongRef implements LongRef$
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
	public long apply()
	{
		return this.value;
	}
	
	@Override
	public void update(long value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return Long.toString(this.value);
	}
}
