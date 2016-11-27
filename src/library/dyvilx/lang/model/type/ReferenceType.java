package dyvilx.lang.model.type;

import dyvil.annotation.internal.ClassParameters;
import dyvil.ref.*;

@ClassParameters(names = { "type" })
public class ReferenceType<T, R> implements Type<R>
{
	protected final Type<T> type;

	protected final Class<R> referenceClass;

	public static <T, R> ReferenceType<T, R> apply(Type<T> type)
	{
		return new ReferenceType<>(type);
	}

	public static <T, R> ReferenceType<T, R> apply(Class<R> referenceClass, Type<T> type)
	{
		return new ReferenceType<>(referenceClass, type);
	}

	public ReferenceType(Type<T> type)
	{
		this.type = type;
		this.referenceClass = (Class<R>) (Class) getErasureClass(type);
	}

	public ReferenceType(Class<R> referenceClass, Type<T> type)
	{
		this.type = type;
		this.referenceClass = referenceClass;
	}

	private static Class<?> getErasureClass(Type<?> type)
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

	public Type<T> type()
	{
		return this.type;
	}

	@Override
	public Class<R> erasure()
	{
		return this.referenceClass;
	}

	@Override
	public int typeArgumentCount()
	{
		return 1;
	}

	@Override
	public <R1> Type<R1> typeArgument(int index)
	{
		return index == 0 ? (Type<R1>) (Type) this.type : null;
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
		builder.append('L').append(this.referenceClass.getCanonicalName().replace('.', '/')).append(';');
	}

	@Override
	public void appendGenericSignature(StringBuilder builder)
	{
		if (this.referenceClass == ObjectRef.class) {
			builder.append('L').append(this.referenceClass.getCanonicalName().replace('.', '/')).append('<');
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
