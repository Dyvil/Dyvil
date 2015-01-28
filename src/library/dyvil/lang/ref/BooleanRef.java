package dyvil.lang.ref;

import dyvil.lang.Boolean;

public class BooleanRef
{
	protected boolean value;
	
	protected BooleanRef(boolean value)
	{
		this.value = value;
	}
	
	public static BooleanRef create(boolean value)
	{
		return new BooleanRef(value);
	}
	
	public Boolean $bang()
	{
		return Boolean.create(!this.value);
	}
	
	public boolean $eq$eq(boolean v)
	{
		return this.value == v;
	}
	
	public boolean $bang$eq(boolean v)
	{
		return this.value != v;
	}
	
	public Boolean $bar(boolean v)
	{
		return Boolean.create(this.value || v);
	}
	
	public Boolean $amp(boolean v)
	{
		return Boolean.create(this.value && v);
	}
	
	public Boolean $up(boolean v)
	{
		return Boolean.create(this.value ^ v);
	}
}
