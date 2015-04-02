package dyvil.lang;

import dyvil.lang.annotation.sealed;

public @sealed interface Boolean$
{
	public boolean booleanValue();
	
	public Boolean$ $bang();
	
	public boolean $eq$eq(boolean v);
	
	public boolean $bang$eq(boolean v);
	
	public Boolean$ $amp(boolean v);
	
	public Boolean$ $bar(boolean v);
	
	public Boolean$ $up(boolean v);
	
	public Boolean$ $eq$eq$gt(boolean v);
	
	public Boolean$ $lt$eq$gt(boolean v);
}
