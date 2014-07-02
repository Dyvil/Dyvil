package dyvil.lang.primitive;

import dyvil.lang.Number;

public class ByteRef extends dyvil.lang.Byte
{
	protected ByteRef(byte value)
	{
		super(value);
	}
	
	public static final ByteRef get(byte value)
	{
		return new ByteRef(value);
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
		return ShortRef.get(v);
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
