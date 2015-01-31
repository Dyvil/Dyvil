package dyvil.lang;

import java.util.NoSuchElementException;

public final class None implements Option
{
	public static final None	instance	= new None();
	
	@Override
	public Object get()
	{
		throw new NoSuchElementException("None");
	}
	
	@Override
	public boolean isEmpty()
	{
		return true;
	}
	
	@Override
	public String toString()
	{
		return "None";
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj == this || (obj != null && this.equals(obj));
	}
	
	@Override
	public int hashCode()
	{
		return 0;
	}
}
