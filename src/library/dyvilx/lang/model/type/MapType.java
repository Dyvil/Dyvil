package dyvilx.lang.model.type;

import dyvil.annotation._internal.ClassParameters;
import dyvil.collection.Map;

@ClassParameters(names = { "keyType", "valueType" })
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
		return (Class<Map<K, V>>) (Class) Map.class;
	}

	public Type<K> keyType()
	{
		return this.keyType;
	}

	public Type<V> valueType()
	{
		return this.valueType;
	}

	@Override
	public int typeArgumentCount()
	{
		return 2;
	}

	@Override
	public <R> Type<R> typeArgument(int index)
	{
		switch (index)
		{
		case 0:
			return (Type<R>) this.keyType;
		case 1:
			return (Type<R>) this.valueType;
		}
		return null;
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
