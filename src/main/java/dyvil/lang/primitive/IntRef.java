package dyvil.lang.primitive;

import dyvil.lang.Number;

public class IntRef extends dyvil.lang.Int
{
	protected IntRef(int value)
	{
		super(value);
	}
	
	public static final IntRef get(int value)
	{
		return new IntRef(value);
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
		return LongRef.get(v);
	}
	
	@Override
	public Number set$(float v)
	{
		return FloatRef.get(v);
	}
	
	@Override
	public Number set$(double v)
	{
		return DoubleRef.get(v);
	}
}
