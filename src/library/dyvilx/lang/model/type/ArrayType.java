package dyvilx.lang.model.type;

import dyvil.array.ObjectArray;
import dyvil.lang.literal.TypeConvertible;

@TypeConvertible
public class ArrayType<T> implements Type<T[]>
{
	protected final Type componentType;
	
	public static <T> ArrayType<T> apply(Type<T> type)
	{
		return new ArrayType(type);
	}
	
	public ArrayType(Type componentType)
	{
		this.componentType = componentType;
	}
	
	@Override
	public Class erasure()
	{
		return ObjectArray.getArrayType(this.componentType.erasure());
	}
	
	@Override
	public String name()
	{
		return this.componentType.name();
	}
	
	@Override
	public String qualifiedName()
	{
		return this.componentType.qualifiedName();
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
		builder.append('[');
		this.componentType.toString(builder);
		builder.append(']');
	}
	
	@Override
	public void appendSignature(StringBuilder builder)
	{
		builder.append('[');
		this.componentType.appendSignature(builder);
	}
	
	@Override
	public void appendGenericSignature(StringBuilder builder)
	{
		builder.append('[');
		this.componentType.appendGenericSignature(builder);
	}
}
