package dyvil.lang.ref.simple;

import dyvil.lang.literal.IntConvertible;
import dyvil.lang.ref.IIntRef;

@IntConvertible
public class IntRef implements IIntRef
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
	public int get()
	{
		return this.value;
	}
	
	@Override
	public void set(int value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return Integer.toString(this.value);
	}
}
