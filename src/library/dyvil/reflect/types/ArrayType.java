package dyvil.reflect.types;

import dyvil.annotation.internal.ClassParameters;
import dyvil.lang.LiteralConvertible;
import dyvil.lang.Types;

@LiteralConvertible.FromType
@ClassParameters(names = { "componentType" })
public class ArrayType<T> implements Type<T[]>
{
	protected final Type<T> componentType;
	
	public static <T> ArrayType<T> apply(Type<T> type)
	{
		return new ArrayType<>(type);
	}
	
	public ArrayType(Type<T> componentType)
	{
		this.componentType = componentType;
	}
	
	@Override
	public Class<T[]> erasure()
	{
		return (Class<T[]>) Types.arrayType(this.componentType.erasure());
	}

	@Override
	public int typeArgumentCount()
	{
		return 1;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Type<?> typeArgument(int index)
	{
		return index != 0 ? null : this.componentType;
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
