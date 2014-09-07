package dyvil.lang.primitive;

import dyvil.lang.Boolean;

public class BooleanRef extends dyvil.lang.Boolean
{
	protected BooleanRef(boolean value)
	{
		super(value);
	}
	
	public static BooleanRef get(boolean value)
	{
		return new BooleanRef(value);
	}
	
	@Override
	public Boolean set$(boolean v)
	{
		this.value = v;
		return this;
	}
}
