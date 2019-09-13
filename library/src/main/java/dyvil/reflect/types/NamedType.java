package dyvil.reflect.types;

import dyvil.annotation.internal.ClassParameters;
import dyvil.lang.LiteralConvertible;

@LiteralConvertible.FromString
@LiteralConvertible.FromClass
@ClassParameters(names = { "theClass" })
public class NamedType<T> implements Type<T>
{
	protected final String   name;
	protected final Class<T> theClass;
	
	public static <T> NamedType<T> apply(String className)
	{
		return new NamedType<>(className);
	}
	
	public static <T> NamedType<T> apply(Class<T> c)
	{
		return new NamedType<>(c);
	}
	
	public NamedType(String name)
	{
		this.name = name;

		Class<T> theClass;
		try
		{
			theClass = (Class<T>) Class.forName(this.name, false, ClassLoader.getSystemClassLoader());
		}
		catch (ClassNotFoundException ignored)
		{
			theClass = null;
		}
		this.theClass = theClass;
	}
	
	public NamedType(Class<T> theClass)
	{
		this.name = theClass.getCanonicalName();
		this.theClass = theClass;
	}

	/*
	 * To satisfy case class pattern matching
	 */
	public Class<T> theClass()
	{
		return this.theClass;
	}
	
	@Override
	public String name()
	{
		return this.erasure().getSimpleName();
	}
	
	@Override
	public String qualifiedName()
	{
		return this.name;
	}
	
	@Override
	public Class<T> erasure()
	{
		return this.theClass;
	}
	
	@Override
	public String toString()
	{
		return this.name;
	}
	
	@Override
	public void toString(StringBuilder builder)
	{
		builder.append(this.name);
	}
	
	@Override
	public void appendSignature(StringBuilder builder)
	{
		builder.append('L').append(this.name.replace('.', '/')).append(';');
	}
}
