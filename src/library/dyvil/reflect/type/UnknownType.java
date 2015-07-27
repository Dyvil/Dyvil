package dyvil.reflect.type;

import dyvil.lang.Type;
import dyvil.lang.literal.NilConvertible;

import dyvil.annotation.object;

@NilConvertible
public @object class UnknownType<T> implements Type<T>
{
	public static final UnknownType instance = new UnknownType();
	
	public static <T> UnknownType<T> apply()
	{
		return instance;
	}
	
	@Override
	public Class getTheClass()
	{
		return null;
	}
	
	@Override
	public String getName()
	{
		return "var";
	}
	
	@Override
	public String getQualifiedName()
	{
		return "var";
	}
	
	@Override
	public String toString()
	{
		return "var";
	}
	
	@Override
	public void appendSignature(StringBuilder builder)
	{
		builder.append("Ljava/lang/Object;");
	}
}
