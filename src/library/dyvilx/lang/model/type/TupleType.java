package dyvilx.lang.model.type;

import dyvil.annotation.internal.ClassParameters;
import dyvil.lang.LiteralConvertible;

@LiteralConvertible.FromTuple
@ClassParameters(names = { "types" })
public class TupleType<T> implements Type<T>
{
	protected final Type<?>[] types;
	protected final Class<T>  tupleClass;
	
	public static <T> TupleType<T> apply(Type<?>... types)
	{
		return new TupleType<>(types);
	}

	public static <T> TupleType<T> apply(Class<T> tupleClass, Type<?>... types)
	{
		return new TupleType<>(tupleClass, types);
	}
	
	public TupleType(Type<?>... types)
	{
		this.types = types;

		Class<T> theClass;
		try
		{
			theClass = (Class<T>) Class.forName(this.qualifiedName());
		}
		catch (ClassNotFoundException ex)
		{
			theClass = null;
		}
		this.tupleClass = theClass;
	}

	public TupleType(Class<T> tupleClass, Type<?>... types)
	{
		this.types = types;
		this.tupleClass = tupleClass;
	}

	public Type<?>[] types()
	{
		return this.types;
	}
	
	@Override
	public Class<T> erasure()
	{
		return this.tupleClass;
	}

	@Override
	public int typeArgumentCount()
	{
		return this.types.length;
	}

	@Override
	public <R> Type<R> typeArgument(int index)
	{
		if (index >= this.types.length) {
			return null;
		}
		return (Type<R>) this.types[index];
	}

	@Override
	public String name()
	{
		return "Tuple" + this.types.length;
	}
	
	@Override
	public String qualifiedName()
	{
		return "dyvil/tuple/Tuple$Of" + this.types.length;
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
		builder.append('(');
		int len = this.types.length;
		if (len > 0)
		{
			this.types[0].toString(builder);
			for (int i = 1; i < len; i++)
			{
				builder.append(", ");
				this.types[i].toString(builder);
			}
		}
		builder.append(')');
	}
	
	@Override
	public void appendSignature(StringBuilder builder)
	{
		builder.append("Ldyvil/tuple/Tuple$Of").append(this.types.length).append(';');
	}
	
	@Override
	public void appendGenericSignature(StringBuilder builder)
	{
		builder.append("Ldyvil/tuple/Tuple$Of").append(this.types.length).append('<');
		int len = this.types.length;
		if (len > 0)
		{
			this.types[0].appendGenericSignature(builder);
			for (int i = 1; i < len; i++)
			{
				this.types[i].appendGenericSignature(builder);
			}
		}
		builder.append('>').append(';');
	}
}
