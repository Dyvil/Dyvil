package dyvil.reflect.types;

import dyvil.lang.LiteralConvertible;

@LiteralConvertible.FromString
@LiteralConvertible.FromClass
public interface Type<T>
{
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
		return c.isPrimitive() ? (Type<T>) PrimitiveType.apply(c) : new NamedType<>(c);
	}

	static <T> Type<T> apply(Class<T> c, Type<?>... generics)
	{
		return new GenericType<>(c, generics);
	}

	Class<T> erasure();

	default int typeArgumentCount()
	{
		return 0;
	}

	default <R> Type<R> typeArgument(int index)
	{
		return null;
	}

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
