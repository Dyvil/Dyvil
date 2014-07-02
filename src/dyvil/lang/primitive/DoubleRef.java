package dyvil.lang.primitive;

import dyvil.lang.Number;

public class DoubleRef extends dyvil.lang.Double
{
	protected DoubleRef(double value)
	{
		super(value);
	}
	
	public static final DoubleRef get(double value)
	{
		return new DoubleRef(value);
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
		this.value = v;
		return this;
	}
}
