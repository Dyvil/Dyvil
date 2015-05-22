package dyvil.lang.ref.simple;

import dyvil.lang.literal.DoubleConvertible;
import dyvil.lang.ref.IDoubleRef;

@DoubleConvertible
public class DoubleRef implements IDoubleRef
{
	public double	value;
	
	public static DoubleRef apply(double value)
	{
		return new DoubleRef(value);
	}
	
	public DoubleRef(double value)
	{
		this.value = value;
	}
	
	@Override
	public double get()
	{
		return this.value;
	}
	
	@Override
	public void set(double value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return Double.toString(this.value);
	}
}
