package dyvil.lang;

public abstract class Boolean
{
	protected boolean value;
	
	protected Boolean(boolean value)
	{
		this.value = value;
	}
	
	public abstract Boolean set$(boolean v);
	
	public boolean eq$(boolean v)
	{
		return this.value == v;
	}
	
	public boolean ue$(boolean v)
	{
		return this.value != v;
	}
	
	public Boolean or$(boolean v)
	{
		return this.set$(this.value || v);
	}
	
	public Boolean and$(boolean v)
	{
		return this.set$(this.value && v);
	}
	
	public Boolean xor$(boolean v)
	{
		return this.set$(this.value ^ v);
	}
}
