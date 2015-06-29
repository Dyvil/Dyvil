package dyvil.reflect.type;

import dyvil.lang.Type;

public class WildcardType<T> implements Type<T>
{
	protected final Type	upperBound;
	protected final Type	lowerBound;
	
	public static <T> WildcardType<T> apply(Type lowerBound, Type upperBounds)
	{
		return new WildcardType(lowerBound, upperBounds);
	}
	
	public WildcardType(Type upperBounds)
	{
		this.upperBound = upperBounds;
		this.lowerBound = null;
	}
	
	public WildcardType(Type lowerBound, Type upperBounds)
	{
		this.lowerBound = lowerBound;
		this.upperBound = upperBounds;
	}
	
	@Override
	public Class<T> getTheClass()
	{
		return null;
	}
	
	@Override
	public String getName()
	{
		return "_";
	}
	
	@Override
	public String getQualifiedName()
	{
		return "_";
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		this.toString(builder);
		return builder.toString();
	}
	
	@Override
	public void toString(StringBuilder builder)
	{
		builder.append('_');
		if (this.lowerBound != null)
		{
			builder.append(" >: ");
			this.lowerBound.toString(builder);
		}
		if (this.upperBound != null)
		{
			builder.append(" <: ");
			this.upperBound.toString(builder);
		}
	}
	
	@Override
	public void appendSignature(StringBuilder builder)
	{
		if (this.lowerBound != null)
		{
			builder.append("Ljava/lang/Object;");
			return;
		}
		if (this.upperBound != null)
		{
			this.upperBound.appendSignature(builder);
		}
	}
	
	@Override
	public void appendGenericSignature(StringBuilder builder)
	{
		if (this.lowerBound != null)
		{
			builder.append('-');
			this.lowerBound.appendGenericSignature(builder);
			return;
		}
		if (this.upperBound != null)
		{
			builder.append('+');
			this.upperBound.appendSignature(builder);
			return;
		}
		builder.append('*');
	}
}
