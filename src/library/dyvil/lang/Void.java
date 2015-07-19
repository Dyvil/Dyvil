package dyvil.lang;

import dyvil.annotation.object;

public final @object class Void
{
	public static final Void	instance	= new Void();
	
	private Void()
	{
		
	}
	
	public static Void apply()
	{
		return instance;
	}
	
	public void unapply()
	{
	}
}
