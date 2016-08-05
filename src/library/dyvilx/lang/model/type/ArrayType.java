package dyvilx.lang.model.type;

import dyvil.annotation._internal.ClassParameters;
import dyvil.array.ObjectArray;
import dyvil.lang.LiteralConvertible;

@LiteralConvertible.FromType
@ClassParameters(names = { "componentType" })
public class ArrayType<T> implements Type<T[]>
{
	protected final Type componentType;
	
	public static <T> ArrayType<T> apply(Type<T> type)
	{
		return new ArrayType<>(type);
	}
	
	public ArrayType(Type componentType)
	{
		this.componentType = componentType;
	}
	
	@Override
	public Class<T[]> erasure()
	{
		return (Class<T[]>) ObjectArray.getArrayType(this.componentType.erasure());
	}

	@Override
	public int typeArgumentCount()
	{
		return 1;
	}

	@Override
	public Type<T[]> typeArgument(int index)
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
