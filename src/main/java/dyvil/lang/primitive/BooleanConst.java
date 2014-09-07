package dyvil.lang.primitive;

import dyvil.lang.Boolean;

public class BooleanConst extends dyvil.lang.Boolean
{
	protected BooleanConst(boolean value)
	{
		super(value);
	}
	
	public static Boolean get(boolean value)
	{
		return ConstPool.getBoolean(value);
	}
	
	@Override
	public Boolean set$(boolean v)
	{
		return get(v);
	}
}
