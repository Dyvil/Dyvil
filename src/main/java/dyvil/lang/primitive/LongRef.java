package dyvil.lang.primitive;

import dyvil.lang.Number;

public class LongRef extends dyvil.lang.Long
{
	protected LongRef(long value)
	{
		super(value);
	}
	
	public static final LongRef get(long value)
	{
		return new LongRef(value);
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
		return FloatRef.get(v);
	}
	
	@Override
	public Number set$(double v)
	{
		return DoubleRef.get(v);
	}
}
