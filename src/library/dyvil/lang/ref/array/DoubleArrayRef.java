package dyvil.lang.ref.array;

import dyvil.lang.ref.IDoubleRef;

public class DoubleArrayRef implements IDoubleRef
{
	protected final double[]	array;
	protected final int		index;
	
	public DoubleArrayRef(double[] array, int index)
	{
		this.array = array;
		this.index = index;
	}
	
	@Override
	public double get()
	{
		return this.array[this.index];
	}
	
	@Override
	public void set(double value)
	{
		this.array[this.index] = value;
	}
}
