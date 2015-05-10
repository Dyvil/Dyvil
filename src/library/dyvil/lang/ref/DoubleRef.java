package dyvil.lang.ref;

import dyvil.lang.literal.DoubleConvertible;

@DoubleConvertible
public class DoubleRef implements DoubleRef$
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
	public double apply()
	{
		return this.value;
	}
	
	@Override
	public void update(double value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return Double.toString(this.value);
	}
}
