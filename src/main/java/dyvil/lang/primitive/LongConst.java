package dyvil.lang.primitive;

import dyvil.lang.Number;

public class LongConst extends dyvil.lang.Long
{
	protected LongConst(long value)
	{
		super(value);
	}
	
	public static LongConst get(long value)
	{
		return ConstPool.getLong(value);
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
		return FloatConst.get(v);
	}
	
	@Override
	public Number set$(double v)
	{
		return DoubleConst.get(v);
	}
}
