package dyvil.lang.primitive;

import dyvil.lang.Number;

public class FloatRef extends dyvil.lang.Float
{
	protected FloatRef(float value)
	{
		super(value);
	}
	
	public static final FloatRef get(float value)
	{
		return new FloatRef(value);
	}
	
	@Override
	public Number set$(byte v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Number set$(short v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Number set$(char v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Number set$(int v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Number set$(long v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Number set$(float v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Number set$(double v)
	{
		return DoubleRef.get(v);
	}
}
