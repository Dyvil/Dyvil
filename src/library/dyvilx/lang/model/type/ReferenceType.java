package dyvilx.lang.model.type;

import dyvil.annotation._internal.ClassParameters;
import dyvil.ref.*;

@ClassParameters(names = { "type" })
public class ReferenceType implements Type
{
	protected final Type type;

	protected final Class erasureClass;

	public static ReferenceType apply(Type type)
	{
		return new ReferenceType(type);
	}

	public ReferenceType(Type type)
	{
		this.type = type;
		this.erasureClass = getErasureClass(type);
	}

	private static Class getErasureClass(Type type)
	{
		if (type == PrimitiveType.BOOLEAN)
		{
			return BooleanRef.class;
		}
		if (type == PrimitiveType.BYTE)
		{
			return ByteRef.class;
		}
		if (type == PrimitiveType.SHORT)
		{
			return ShortRef.class;
		}
		if (type == PrimitiveType.CHAR)
		{
			return CharRef.class;
		}
		if (type == PrimitiveType.INT)
		{
			return IntRef.class;
		}
		if (type == PrimitiveType.LONG)
		{
			return LongRef.class;
		}
		if (type == PrimitiveType.FLOAT)
		{
			return FloatRef.class;
		}
		if (type == PrimitiveType.DOUBLE)
		{
			return DoubleRef.class;
		}
		return ObjectRef.class;
	}

	public Type type()
	{
		return this.type;
	}

	@Override
	public Class erasure()
	{
		return this.erasureClass;
	}

	@Override
	public String name()
	{
		return this.erasure().getName();
	}

	@Override
	public String qualifiedName()
	{
		return this.erasure().getCanonicalName();
	}

	@Override
	public void appendSignature(StringBuilder builder)
	{
		builder.append('L').append(this.erasureClass.getCanonicalName().replace('.', '/')).append(';');
	}

	@Override
	public void appendGenericSignature(StringBuilder builder)
	{
		if (this.erasureClass == ObjectRef.class) {
			builder.append('L').append(this.erasureClass.getCanonicalName().replace('.', '/')).append('<');
			this.type.appendGenericSignature(builder);
			builder.append(">;");
			return;
		}

		this.appendSignature(builder);
	}

	@Override
	public String toString()
	{
		return this.type.toString() + "*";
	}

	@Override
	public void toString(StringBuilder builder)
	{
		this.type.toString(builder);
		builder.append('*');
	}
}
