package dyvil.lang.primitive;

import dyvil.lang.Number;

public class DoubleConst extends dyvil.lang.Double
{
	protected DoubleConst(double value)
	{
		super(value);
	}
	
	public static DoubleConst get(double value)
	{
		return ConstPool.getDouble(value);
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
		return get(v);
	}
}
