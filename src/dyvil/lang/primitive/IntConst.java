package dyvil.lang.primitive;

import dyvil.lang.Int;
import dyvil.lang.Number;

public class IntConst extends Int
{
	private static IntConst[]	constTable	= new IntConst[constantTableSize];
	
	static
	{
		for (int i = 0; i < constantTableSize; i++)
		{
			constTable[i] = new IntConst(i);
		}
	}
	
	protected IntConst(int value)
	{
		super(value);
	}
	
	public static IntConst get(int value)
	{
		if (value >= 0 && value < constantTableSize)
			return constTable[value];
		return new IntConst(value);
	}
	
	@Override
	public Number $neg$()
	{
		return get(-this.value);
	}
	
	@Override
	public Number $inv$()
	{
		return get(~this.value);
	}
	
	@Override
	public Number $inc$()
	{
		return get(this.value + 1);
	}
	
	@Override
	public Number $dec$()
	{
		return get(this.value - 1);
	}
	
	@Override
	public Number $sqr$()
	{
		return get(this.value * this.value);
	}
	
	@Override
	public Number $rec$()
	{
		return get(1 / this.value);
	}
	
	@Override
	public Int $add$(int i)
	{
		return get(this.value + i);
	}
	
	@Override
	public Int $sub$(int i)
	{
		return get(this.value - i);
	}
	
	@Override
	public Int $mul$(int i)
	{
		return get(this.value * i);
	}
	
	@Override
	public Int $div$(int i)
	{
		return get(this.value / i);
	}
	
	@Override
	public Int $mod$(int i)
	{
		return get(this.value % i);
	}
	
	@Override
	public Number $bsl$(int i)
	{
		return get(this.value << i);
	}
	
	@Override
	public Number $bsr$(int i)
	{
		return get(this.value >> i);
	}
	
	@Override
	public Number $usr$(int i)
	{
		return get(this.value >>> i);
	}
}
