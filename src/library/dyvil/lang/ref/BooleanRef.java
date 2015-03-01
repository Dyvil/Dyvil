package dyvil.lang.ref;

import dyvil.lang.Boolean;
import dyvil.lang.Boolean$;

public class BooleanRef implements Boolean$
{
	protected boolean	value;
	
	protected BooleanRef(boolean value)
	{
		this.value = value;
	}
	
	public static BooleanRef create(boolean value)
	{
		return new BooleanRef(value);
	}
	
	@Override
	public boolean booleanValue()
	{
		return this.value;
	}
	
	@Override
	public Boolean $bang()
	{
		return Boolean.create(!this.value);
	}
	
	@Override
	public boolean $eq$eq(boolean v)
	{
		return this.value == v;
	}
	
	@Override
	public boolean $bang$eq(boolean v)
	{
		return this.value != v;
	}
	
	@Override
	public Boolean $amp(boolean v)
	{
		return Boolean.create(this.value && v);
	}
	
	@Override
	public Boolean $bar(boolean v)
	{
		return Boolean.create(this.value || v);
	}
	
	@Override
	public Boolean $up(boolean v)
	{
		return Boolean.create(this.value ^ v);
	}
	
	public BooleanRef $amp$eq(boolean v)
	{
		this.value &= v;
		return this;
	}
	
	public BooleanRef $bar$eq(boolean v)
	{
		this.value |= v;
		return this;
	}
	
	public BooleanRef $up$eq(boolean v)
	{
		this.value ^= v;
		return this;
	}
	
	@Override
	public String toString()
	{
		return this.value ? "true" : "false";
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj != null && obj.equals(this.value);
	}
	
	@Override
	public int hashCode()
	{
		return this.value ? 1237 : 1231;
	}
}
