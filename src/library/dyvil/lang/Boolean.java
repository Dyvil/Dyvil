package dyvil.lang;

import dyvil.lang.annotation.prefix;

public abstract class Boolean
{
	protected boolean	value;
	
	protected Boolean(boolean value)
	{
		this.value = value;
	}
	
	public abstract Boolean $eq(boolean v);
	
	public @prefix Boolean $bang()
	{
		return this.$eq(!this.value);
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
		return this.$eq(this.value || v);
	}
	
	public Boolean $amp(boolean v)
	{
		return this.$eq(this.value && v);
	}
	
	public Boolean $up(boolean v)
	{
		return this.$eq(this.value ^ v);
	}
}
