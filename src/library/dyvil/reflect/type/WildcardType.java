package dyvil.reflect.type;

import dyvil.lang.Type;

public class WildcardType<T> implements Type<T>
{
	protected final Type[]	upperBounds;
	protected final Type	lowerBound;
	
	public static <T> WildcardType<T> apply(Type lowerBound, Type... upperBounds)
	{
		return new WildcardType(lowerBound, upperBounds);
	}
	
	public WildcardType(Type lowerBound)
	{
		this.lowerBound = lowerBound;
		this.upperBounds = null;
	}
	
	public WildcardType(Type... upperBounds)
	{
		this.upperBounds = upperBounds;
		this.lowerBound = null;
	}
	
	public WildcardType(Type lowerBound, Type... upperBounds)
	{
		this.lowerBound = lowerBound;
		this.upperBounds = upperBounds;
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
			builder.append(" <= ");
			this.lowerBound.toString(builder);
		}
		if (this.upperBounds == null)
		{
			return;
		}
		
		int len = this.upperBounds.length;
		if (len == 0)
		{
			return;
		}
		
		builder.append(" >= ");
		this.upperBounds[0].toString(builder);
		for (int i = 1; i < len; i++)
		{
			builder.append(" & ");
			this.upperBounds[i].toString(builder);
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
		if (this.upperBounds != null && this.upperBounds.length > 0)
		{
			this.upperBounds[0].appendSignature(builder);
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
		if (this.upperBounds != null && this.upperBounds.length > 0)
		{
			this.upperBounds[0].appendSignature(builder);
		}
	}
}
