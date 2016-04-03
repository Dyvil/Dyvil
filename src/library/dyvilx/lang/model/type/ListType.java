package dyvilx.lang.model.type;

import dyvil.annotation._internal.ClassParameters;
import dyvil.collection.List;

@ClassParameters(names = { "elementType" })
public class ListType<E> implements Type<List<E>>
{
	private Type<E> elementType;

	public static <E> ListType<E> apply(Type<E> elementType)
	{
		return new ListType<>(elementType);
	}

	public ListType(Type<E> elementType)
	{
		this.elementType = elementType;
	}
	
	@Override
	public Class<List<E>> erasure()
	{
		return (Class<List<E>>) (Class) List.class;
	}

	public Type<E> elementType()
	{
		return this.elementType;
	}

	@Override
	public int typeArgumentCount()
	{
		return 0;
	}

	@Override
	public <R> Type<R> typeArgument(int index)
	{
		return index == 0 ? (Type<R>) this.elementType : null;
	}

	@Override
	public String name()
	{
		return "List";
	}
	
	@Override
	public String qualifiedName()
	{
		return "dyvil.collection.List";
	}
	
	@Override
	public void appendSignature(StringBuilder builder)
	{
		builder.append("Ldyvil/collection/List;");
	}
	
	@Override
	public void appendGenericSignature(StringBuilder builder)
	{
		builder.append("Ldyvil/collection/List<");
		this.elementType.appendSignature(builder);
		builder.append(">;");
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		this.toString(sb);
		return sb.toString();
	}
	
	@Override
	public void toString(StringBuilder builder)
	{
		builder.append('[');
		this.elementType.toString(builder);
		builder.append("...]");
	}
}
