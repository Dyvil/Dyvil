package dyvil.lang.ref;

import dyvil.lang.literal.IntConvertible;

@IntConvertible
public class IntRef implements IntRef$
{
	public int	value;
	
	public static IntRef apply(int value)
	{
		return new IntRef(value);
	}
	
	public IntRef(int value)
	{
		this.value = value;
	}
	
	@Override
	public int apply()
	{
		return this.value;
	}
	
	@Override
	public void update(int value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return Integer.toString(this.value);
	}
}
