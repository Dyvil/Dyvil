package dyvil.lang.primitive;

import dyvil.lang.Number;

public class CharConst extends dyvil.lang.Char
{
	protected CharConst(char value)
	{
		super(value);
	}
	
	public static CharConst get(char value)
	{
		return ConstPool.getChar(value);
	}
	
	@Override
	public Number set$(byte v)
	{
		return get((char) v);
	}
	
	@Override
	public Number set$(short v)
	{
		return get((char) v);
	}
	
	@Override
	public Number set$(char v)
	{
		return get(v);
	}
	
	@Override
	public Number set$(int v)
	{
		return IntConst.get(v);
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
