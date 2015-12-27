package dyvilx.lang.model.type;

import dyvil.lang.literal.ClassConvertible;
import dyvil.lang.literal.StringConvertible;

@StringConvertible
@ClassConvertible
public class GenericType<T> extends NamedType<T>
{
	protected final Type[] generics;
	
	public static <T> GenericType<T> apply(String className)
	{
		return new GenericType(className);
	}
	
	public static <T> GenericType<T> apply(String className, Type... generics)
	{
		return new GenericType(className, generics);
	}
	
	public static <T> GenericType apply(Class<T> c)
	{
		return new GenericType(c);
	}
	
	public static <T> GenericType<T> apply(Class<T> c, Type... generics)
	{
		return new GenericType(c, generics);
	}
	
	public GenericType(String className, Type... generics)
	{
		super(className);
		this.generics = generics;
	}
	
	public GenericType(Class<T> theClass, Type... generics)
	{
		super(theClass);
		this.generics = generics;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(this.name.length() + this.generics.length * 10);
		this.toString(builder);
		return builder.toString();
	}
	
	@Override
	public void toString(StringBuilder builder)
	{
		builder.append(this.name);
		int len = this.generics.length;
		if (len > 0)
		{
			builder.append('[');
			this.generics[0].toString(builder);
			for (int i = 1; i < len; i++)
			{
				builder.append(", ");
				this.generics[i].toString(builder);
			}
			builder.append(']');
		}
	}
	
	@Override
	public void appendGenericSignature(StringBuilder builder)
	{
		builder.append('L').append(this.name.replace('.', '/'));
		int len = this.generics.length;
		if (len > 0)
		{
			builder.append('<');
			for (int i = 0; i < len; i++)
			{
				this.generics[i].appendGenericSignature(builder);
			}
			builder.append('>');
		}
		builder.append(';');
	}
}
