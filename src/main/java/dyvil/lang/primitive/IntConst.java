package dyvil.lang.primitive;

import dyvil.lang.Number;

public class IntConst extends dyvil.lang.Int
{
	protected IntConst(int value)
	{
		super(value);
	}
	
	public static IntConst get(int value)
	{
		return ConstPool.getInt(value);
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
		return LongConst.get(v);
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
