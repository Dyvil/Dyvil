package dyvil.lang;

import dyvil.annotation._internal.object;

import java.io.Serializable;

public final
@object
class Void implements Serializable
{
	private static final long serialVersionUID = -7512474716905358710L;
	
	public static final Void instance = new Void();
	
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
	
	@Override
	public String toString()
	{
		return "Void";
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj == instance;
	}
	
	@Override
	public int hashCode()
	{
		return 0;
	}
	
	private Object writeReplace() throws java.io.ObjectStreamException
	{
		return instance;
	}
	
	private Object readResolve() throws java.io.ObjectStreamException
	{
		return instance;
	}
}
