package dyvil.lang.ref.simple;

import dyvil.lang.literal.FloatConvertible;
import dyvil.lang.ref.IFloatRef;

@FloatConvertible
public class FloatRef implements IFloatRef
{
	public float	value;
	
	public static FloatRef apply(float value)
	{
		return new FloatRef(value);
	}
	
	public FloatRef(float value)
	{
		this.value = value;
	}
	
	@Override
	public float get()
	{
		return this.value;
	}
	
	@Override
	public void set(float value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return Float.toString(this.value);
	}
}
