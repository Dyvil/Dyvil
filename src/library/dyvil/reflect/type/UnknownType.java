package dyvil.reflect.type;

import dyvil.lang.Type;
import dyvil.lang.literal.NilConvertible;

@NilConvertible
public class UnknownType<T> implements Type<T>
{
	private static final UnknownType	unknownType	= new UnknownType();
	
	public static <T> UnknownType<T> apply()
	{
		return unknownType;
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
