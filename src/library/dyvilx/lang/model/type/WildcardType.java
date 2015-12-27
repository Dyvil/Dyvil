package dyvilx.lang.model.type;

import dyvil.reflect.Variance;

public class WildcardType<T> implements Type<T>
{
	protected final Variance variance;
	protected final Type     bound;
	
	public static <T> WildcardType<T> apply(Variance variance, Type upperBounds)
	{
		return new WildcardType(variance, upperBounds);
	}
	
	public WildcardType(Variance variance, Type bound)
	{
		this.variance = variance;
		this.bound = bound;
	}
	
	@Override
	public Class<T> erasure()
	{
		return null;
	}
	
	@Override
	public String name()
	{
		return "_";
	}
	
	@Override
	public String qualifiedName()
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
		if (this.bound != null)
		{
			if (this.variance == Variance.CONTRAVARIANT)
			{
				builder.append(" >: ");
			}
			else
			{
				builder.append(" <: ");
			}
			this.bound.toString(builder);
		}
	}
	
	@Override
	public void appendSignature(StringBuilder builder)
	{
		if (this.bound != null && this.variance == Variance.COVARIANT)
		{
			this.bound.appendSignature(builder);
			return;
		}
		builder.append("Ljava/lang/Object;");
	}
	
	@Override
	public void appendGenericSignature(StringBuilder builder)
	{
		if (this.bound != null)
		{
			if (this.variance == Variance.CONTRAVARIANT)
			{
				builder.append('-');
			}
			else
			{
				builder.append('+');
			}
			this.bound.appendSignature(builder);
			return;
		}
		builder.append('*');
	}
}
