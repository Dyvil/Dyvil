package dyvil.lang.primitive;

import dyvil.lang.Number;

public class ShortRef extends dyvil.lang.Short
{
	protected ShortRef(short value)
	{
		super(value);
	}
	
	public static final ShortRef get(short value)
	{
		return new ShortRef(value);
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
		return CharRef.get(v);
	}
	
	@Override
	public Number set$(int v)
	{
		return IntRef.get(v);
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
