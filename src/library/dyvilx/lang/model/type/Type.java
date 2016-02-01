package dyvilx.lang.model.type;

import dyvil.lang.literal.ClassConvertible;
import dyvil.lang.literal.NilConvertible;
import dyvil.lang.literal.StringConvertible;

@NilConvertible
@StringConvertible
@ClassConvertible
public interface Type<T>
{
	static <T> Type<T> apply()
	{
		return (Type<T>) UnknownType.instance;
	}
	
	static <T> Type<T> apply(String className)
	{
		return new NamedType<>(className);
	}
	
	static <T> Type<T> apply(String className, Type<?>... generics)
	{
		return new GenericType<>(className, generics);
	}
	
	static <T> Type<T> apply(Class<T> c)
	{
		if (c.isPrimitive())
		{
			return (Type<T>) PrimitiveType.apply(c);
		}
		return new NamedType<>(c);
	}
	
	static <T> Type<T> apply(Class<T> c, Type<?>... generics)
	{
		return new GenericType<>(c, generics);
	}
	
	Class<T> erasure();
	
	String name();
	
	String qualifiedName();
	
	@Override
	String toString();
	
	default void toString(StringBuilder builder)
	{
		builder.append(this.toString());
	}
	
	default String getSignature()
	{
		StringBuilder builder = new StringBuilder();
		this.appendSignature(builder);
		return builder.toString();
	}
	
	void appendSignature(StringBuilder builder);
	
	default String getGenericSignature()
	{
		StringBuilder builder = new StringBuilder();
		this.appendGenericSignature(builder);
		return builder.toString();
	}
	
	default void appendGenericSignature(StringBuilder builder)
	{
		this.appendSignature(builder);
	}
}
