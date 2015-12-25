package dyvil.reflect.types;

import dyvil.collection.Map;
import dyvil.lang.Type;

public class MapType<K, V> implements Type<Map<K, V>>
{
	private Type<K> keyType;
	private Type<V> valueType;
	
	public static <K, V> MapType<K, V> apply(Type<K> keyType, Type<V> valueType)
	{
		return new MapType<K, V>(keyType, valueType);
	}
	
	public MapType(Type<K> keyType, Type<V> valueType)
	{
		this.keyType = keyType;
		this.valueType = valueType;
	}
	
	@Override
	public Class<Map<K, V>> erasure()
	{
		return (Class) Map.class;
	}
	
	@Override
	public String name()
	{
		return "Map";
	}
	
	@Override
	public String qualifiedName()
	{
		return "dyvil.collection.Map";
	}
	
	@Override
	public void appendSignature(StringBuilder builder)
	{
		builder.append("Ldyvil/collection/Map;");
	}
	
	@Override
	public void appendGenericSignature(StringBuilder builder)
	{
		builder.append("Ldyvil/collection/Map<");
		this.keyType.appendSignature(builder);
		this.valueType.appendSignature(builder);
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
		this.keyType.toString(builder);
		builder.append(':');
		this.valueType.toString(builder);
		builder.append(']');
	}
}
