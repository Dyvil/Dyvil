package dyvil.lang;

public abstract class Byte extends Number
{
	protected byte value;
	
	protected Byte(byte value)
	{
		this.value = value;
	}
	
	@Override
	public byte byteValue()
	{
		return this.value;
	}
	
	@Override
	public short shortValue()
	{
		return (short) this.value;
	}
	
	@Override
	public char charValue()
	{
		return (char) this.value;
	}
	
	@Override
	public int intValue()
	{
		return (int) this.value;
	}
	
	@Override
	public long longValue()
	{
		return (long) this.value;
	}
	
	@Override
	public float floatValue()
	{
		return (float) this.value;
	}
	
	@Override
	public double doubleValue()
	{
		return (double) this.value;
	}
}
