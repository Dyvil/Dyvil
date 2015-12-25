package dyvil.reflect.types;

import dyvil.collection.List;
import dyvil.lang.Type;

public class ListType<E> implements Type<List<E>>
{
	private Type<E> elementType;

	public static <E> ListType<E> apply(Type<E> elementType)
	{
		return new ListType<E>(elementType);
	}

	public ListType(Type<E> elementType)
	{
		this.elementType = elementType;
	}
	
	@Override
	public Class<List<E>> getTheClass()
	{
		return (Class) List.class;
	}
	
	@Override
	public String getName()
	{
		return "List";
	}
	
	@Override
	public String getQualifiedName()
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
