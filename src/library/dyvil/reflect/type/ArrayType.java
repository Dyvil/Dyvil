package dyvil.reflect.type;

import dyvil.lang.Type;
import dyvil.lang.literal.TypeConvertible;

import dyvil.array.ObjectArray;

@TypeConvertible
public class ArrayType<T> implements Type<T[]>
{
	protected final Type	componentType;
	
	public static <T> ArrayType<T> apply(Type<T> type)
	{
		return new ArrayType(type);
	}
	
	public ArrayType(Type componentType)
	{
		this.componentType = componentType;
	}
	
	@Override
	public Class getTheClass()
	{
		return ObjectArray.getArrayType(this.componentType.getTheClass());
	}
	
	@Override
	public String getName()
	{
		return this.componentType.getName();
	}
	
	@Override
	public String getQualifiedName()
	{
		return this.componentType.getQualifiedName();
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
