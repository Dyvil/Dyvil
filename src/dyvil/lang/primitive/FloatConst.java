package dyvil.lang.primitive;

import dyvil.lang.Number;

public class FloatConst extends dyvil.lang.Float
{
	protected FloatConst(float value)
	{
		super(value);
	}
	
	public static FloatConst get(float value)
	{
		return ConstPool.getFloat(value);
	}
	
	@Override
	public Number set$(byte v)
	{
		return get(v);
	}
	
	@Override
	public Number set$(short v)
	{
		return get(v);
	}
	
	@Override
	public Number set$(char v)
	{
		return get(v);
	}
	
	@Override
	public Number set$(int v)
	{
		return get(v);
	}
	
	@Override
	public Number set$(long v)
	{
		return get(v);
	}
	
	@Override
	public Number set$(float v)
	{
		return get(v);
	}
	
	@Override
	public Number set$(double v)
	{
		return DoubleConst.get(v);
	}
}
