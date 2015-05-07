package dyvil.lang;

import dyvil.lang.literal.ClassConvertible;
import dyvil.lang.literal.StringConvertible;
import dyvil.reflect.type.GenericType;
import dyvil.reflect.type.NamedType;
import dyvil.reflect.type.PrimitiveType;

@StringConvertible
@ClassConvertible
public interface Type<T>
{
	public static <T> Type<T> apply(String className)
	{
		return new NamedType(className);
	}
	
	public static <T> Type<T> apply(String className, Type... generics)
	{
		return new GenericType(className, generics);
	}
	
	public static <T> Type<T> apply(Class<T> c)
	{
		if (c.isPrimitive())
		{
			return new PrimitiveType(c);
		}
		return new NamedType(c);
	}
	
	public static <T> Type<T> apply(Class<T> c, Type... generics)
	{
		return new GenericType(c, generics);
	}
	
	public Class<T> getTheClass();

	public String getName();
	
	public String getQualifiedName();
	
	@Override
	public String toString();
	
	public default void toString(StringBuilder builder)
	{
		builder.append(this.toString());
	}
	
	public default String getSignature()
	{
		StringBuilder builder = new StringBuilder();
		this.appendSignature(builder);
		return builder.toString();
	}
	
	public void appendSignature(StringBuilder builder);
	
	public default String getGenericSignature()
	{
		StringBuilder builder = new StringBuilder();
		this.appendGenericSignature(builder);
		return builder.toString();
	}
	
	public default void appendGenericSignature(StringBuilder builder)
	{
		this.appendSignature(builder);
	}
}
