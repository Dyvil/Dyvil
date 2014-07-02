package dyvil.lang.primitive;

import dyvil.lang.Int;
import dyvil.lang.Number;

public class IntRef extends Int
{
	protected IntRef(int value)
	{
		super(value);
	}
	
	public static IntRef get(int value)
	{
		return new IntRef(value);
	}
	
	@Override
	public Number $inc$()
	{
		this.value++;
		return this;
	}
	
	@Override
	public Number $dec$()
	{
		this.value--;
		return this;
	}
	
	@Override
	public Number $sqr$()
	{
		this.value *= this.value;
		return this;
	}
	
	@Override
	public Number $rec$()
	{
		this.value = 1 / this.value;
		return this;
	}

	@Override
	public IntRef $add$(int i)
	{
		this.value += i;
		return this;
	}

	@Override
	public IntRef $sub$(int i)
	{
		this.value -= i;
		return this;
	}

	@Override
	public IntRef $mul$(int i)
	{
		this.value *= i;
		return this;
	}

	@Override
	public IntRef $div$(int i)
	{
		this.value /= i;
		return this;
	}

	@Override
	public IntRef $mod$(int i)
	{
		this.value %= i;
		return this;
	}
	
	@Override
	public Number $bsl$(int i)
	{
		this.value <<= i;
		return this;
	}
	
	@Override
	public Number $bsr$(int i)
	{
		this.value >>= i;
		return this;
	}
	
	@Override
	public Number $usr$(int i)
	{
		this.value >>>= i;
		return this;
	}
}
