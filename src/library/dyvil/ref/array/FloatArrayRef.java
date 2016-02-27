package dyvil.ref.array;

import dyvil.ref.FloatRef;

public class FloatArrayRef implements FloatRef
{
	protected final float[] array;
	protected final int     index;
	
	public FloatArrayRef(float[] array, int index)
	{
		this.array = array;
		this.index = index;
	}
	
	@Override
	public float get()
	{
		return this.array[this.index];
	}
	
	@Override
	public void set(float value)
	{
		this.array[this.index] = value;
	}
}
