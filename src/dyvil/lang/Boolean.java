package dyvil.lang;

import dyvil.lang.annotation.bytecode;

public abstract class Boolean
{
	protected boolean value;
	
	protected Boolean(boolean value)
	{
		this.value = value;
	}
	
	public abstract Boolean set$(boolean v);
	
	@bytecode("==")
	public boolean eq$(boolean v)
	{
		return this.value == v;
	}
	
	@bytecode("!=")
	public boolean ue$(boolean v)
	{
		return this.value != v;
	}
	
	@bytecode("|")
	public Boolean or$(boolean v)
	{
		return this.set$(this.value || v);
	}
	
	@bytecode("&")
	public Boolean and$(boolean v)
	{
		return this.set$(this.value && v);
	}
	
	@bytecode("^")
	public Boolean xor$(boolean v)
	{
		return this.set$(this.value ^ v);
	}
}
