package dyvilx.lang.model.type;

import dyvil.annotation._internal.ClassParameters;
import dyvil.lang.LiteralConvertible;

@LiteralConvertible.FromString
@LiteralConvertible.FromClass
@ClassParameters(names = { "theClass", "typeArguments" })
public class GenericType<T> extends NamedType<T>
{
	protected final Type[] typeArguments;

	@SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
	public static <T> GenericType<T> apply(String className)
	{
		return new GenericType<>(className);
	}

	public static <T> GenericType<T> apply(String className, Type<?>... generics)
	{
		return new GenericType<>(className, generics);
	}

	@SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
	public static <T> GenericType apply(Class<T> c)
	{
		return new GenericType<>(c);
	}

	public static <T> GenericType<T> apply(Class<T> c, Type<?>... generics)
	{
		return new GenericType<>(c, generics);
	}

	public GenericType(String className, Type<?>... generics)
	{
		super(className);
		this.typeArguments = generics;
	}

	public GenericType(Class<T> theClass, Type<?>... generics)
	{
		super(theClass);
		this.typeArguments = generics;
	}

	public Type<?>[] typeArguments()
	{
		return this.typeArguments;
	}

	@Override
	public int typeArgumentCount()
	{
		return this.typeArguments.length;
	}

	@Override
	public <R> Type<R> typeArgument(int index)
	{
		if (index >= this.typeArguments.length)
		{
			return null;
		}
		return (Type<R>) this.typeArguments[index];
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(this.name.length() + this.typeArguments.length * 10);
		this.toString(builder);
		return builder.toString();
	}

	@Override
	public void toString(StringBuilder builder)
	{
		builder.append(this.name);

		final int count = this.typeArguments.length;
		if (count > 0)
		{
			builder.append('<');
			this.typeArguments[0].toString(builder);
			for (int i = 1; i < count; i++)
			{
				builder.append(", ");
				this.typeArguments[i].toString(builder);
			}
			builder.append('>');
		}
	}

	@Override
	public void appendGenericSignature(StringBuilder builder)
	{
		builder.append('L').append(this.name.replace('.', '/'));
		int len = this.typeArguments.length;
		if (len > 0)
		{
			builder.append('<');
			for (Type generic : this.typeArguments)
			{
				generic.appendGenericSignature(builder);
			}
			builder.append('>');
		}
		builder.append(';');
	}
}
