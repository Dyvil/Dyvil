package dyvil.reflect.types;

import dyvil.lang.Type;
import dyvil.lang.literal.ClassConvertible;
import dyvil.lang.literal.StringConvertible;

@StringConvertible
@ClassConvertible
public class NamedType<T> implements Type<T>
{
	protected final String	name;
	protected Class<T>		theClass;
	
	public static <T> NamedType<T> apply(String className)
	{
		return new NamedType(className);
	}
	
	public static <T> NamedType<T> apply(Class<T> c)
	{
		return new NamedType(c);
	}
	
	public NamedType(final String name)
	{
		this.name = name;
	}
	
	public NamedType(final Class<T> theClass)
	{
		this.name = theClass.getCanonicalName();
		this.theClass = theClass;
	}
	
	@Override
	public String getName()
	{
		return this.getTheClass().getSimpleName();
	}
	
	@Override
	public String getQualifiedName()
	{
		return this.name;
	}
	
	@Override
	public Class<T> getTheClass()
	{
		if (this.theClass == null)
		{
			try
			{
				return this.theClass = (Class<T>) Class.forName(this.name, false, ClassLoader.getSystemClassLoader());
			}
			catch (ClassNotFoundException ex)
			{
				return null;
			}
		}
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
