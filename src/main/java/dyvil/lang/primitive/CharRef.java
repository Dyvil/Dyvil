package dyvil.lang.primitive;

import dyvil.lang.Number;

public class CharRef extends dyvil.lang.Char
{
	protected CharRef(char value)
	{
		super(value);
	}
	
	public static final CharRef get(char value)
	{
		return new CharRef(value);
	}
	
	@Override
	public Number set$(byte v)
	{
		this.value = (char) v;
		return this;
	}
	
	@Override
	public Number set$(short v)
	{
		this.value = (char) v;
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
