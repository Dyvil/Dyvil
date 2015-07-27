package dyvil.lang.ref.simple;

import dyvil.lang.literal.DoubleConvertible;
import dyvil.lang.ref.DoubleRef;

@DoubleConvertible
public class SimpleDoubleRef implements DoubleRef
{
	public double value;
	
	public static SimpleDoubleRef apply(double value)
	{
		return new SimpleDoubleRef(value);
	}
	
	public SimpleDoubleRef(double value)
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
