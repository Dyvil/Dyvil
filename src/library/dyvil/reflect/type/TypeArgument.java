package dyvil.reflect.type;

import dyvil.lang.Type;
import dyvil.lang.literal.StringConvertible;

@StringConvertible
public class TypeArgument implements Type
{
	private final String name;
	
	public static TypeArgument apply(String name)
	{
		return new TypeArgument(name);
	}
	
	public TypeArgument(String name)
	{
		this.name = name;
	}
	
	@Override
	public Class getTheClass()
	{
		return null;
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public String getQualifiedName()
	{
		return this.name;
	}
	
	@Override
	public String toString()
	{
		return this.name;
	}
	
	@Override
	public void appendSignature(StringBuilder builder)
	{
		builder.append('T').append(this.name).append(';');
	}
}
