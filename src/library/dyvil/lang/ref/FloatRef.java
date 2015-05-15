package dyvil.lang.ref;

import dyvil.lang.literal.FloatConvertible;

@FloatConvertible
public class FloatRef implements FloatRef$
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
	public float apply()
	{
		return this.value;
	}
	
	@Override
	public void update(float value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return Float.toString(this.value);
	}
}
