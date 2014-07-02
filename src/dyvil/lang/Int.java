package dyvil.lang;

public abstract class Int extends Number
{
	protected int value;
	
	protected Int(int value)
	{
		this.value = value;
	}
	
	@Override
	public int intValue()
	{
		return this.value;
	}
}
