package dyvil.lang;

import dyvil.lang.annotation.bytecode;
import dyvil.lang.annotation.prefix;

public abstract class Boolean
{
	protected boolean	value;
	
	protected Boolean(boolean value)
	{
		this.value = value;
	}
	
	public abstract Boolean $eq(boolean v);
	
	@bytecode("!")
	public @prefix Boolean $bang()
	{
		return this.$eq(!this.value);
	}
	
	@bytecode("==")
	public boolean $eq$eq(boolean v)
	{
		return this.value == v;
	}
	
	@bytecode("!=")
	public boolean $bang$eq(boolean v)
	{
		return this.value != v;
	}
	
	@bytecode("|")
	public Boolean $bar(boolean v)
	{
		return this.$eq(this.value || v);
	}
	
	@bytecode("&")
	public Boolean $amp(boolean v)
	{
		return this.$eq(this.value && v);
	}
	
	@bytecode("^")
	public Boolean $up(boolean v)
	{
		return this.$eq(this.value ^ v);
	}
}
