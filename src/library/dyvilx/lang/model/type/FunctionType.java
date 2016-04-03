package dyvilx.lang.model.type;

import dyvil.annotation._internal.ClassParameters;
import dyvil.lang.literal.TupleConvertible;

@TupleConvertible
@ClassParameters(names = { "returnType", "parameterTypes" })
public class FunctionType<F> implements Type<F>
{
	protected final Type<?>   returnType;
	protected final Type<?>[] parameterTypes;
	protected final Class     functionType;

	public static <F> FunctionType<F> apply(Type<?> returnType, Type<?>... parameterTypes)
	{
		return new FunctionType<>(returnType, parameterTypes);
	}

	public static <F> FunctionType<F> apply(Class<F> functionType, Type<?> returnType, Type<?>... parameterTypes)
	{
		return new FunctionType<>(functionType, returnType, parameterTypes);
	}

	public FunctionType(Type<?> returnType, Type<?>... parameterTypes)
	{
		this.returnType = returnType;
		this.parameterTypes = parameterTypes;

		Class<?> theClass;
		try
		{
			theClass = Class.forName(this.qualifiedName());
		}
		catch (ClassNotFoundException ex)
		{
			theClass = null;
		}
		this.functionType = theClass;
	}

	public FunctionType(Class<F> functionType, Type<?> returnType, Type<?>... parameterTypes)
	{
		this.returnType = returnType;
		this.parameterTypes = parameterTypes;
		this.functionType = functionType;
	}

	@Override
	public Class<F> erasure()
	{
		return (Class<F>) this.functionType;
	}

	public <R> Type<R> returnType()
	{
		return (Type<R>) this.returnType;
	}

	public Type<?>[] parameterTypes()
	{
		return this.parameterTypes;
	}

	@Override
	public int typeArgumentCount()
	{
		return 1 + this.parameterTypes.length;
	}

	@Override
	public <R> Type<R> typeArgument(int index)
	{
		final int types = this.parameterTypes.length;
		if (index == types)
		{
			return (Type<R>) this.returnType;
		}
		if (index > types)
		{
			return null;
		}
		return (Type<R>) this.parameterTypes[index];
	}

	@Override
	public String name()
	{
		return "Function" + this.parameterTypes.length;
	}

	@Override
	public String qualifiedName()
	{
		return "dyvil/function/Function" + this.parameterTypes.length;
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
		int len = this.parameterTypes.length;
		if (len > 0)
		{
			this.parameterTypes[0].toString(builder);
			for (int i = 1; i < len; i++)
			{
				builder.append(", ");
				this.parameterTypes[i].toString(builder);
			}
		}
		builder.append(") => ");
		this.returnType.toString(builder);
	}

	@Override
	public void appendSignature(StringBuilder builder)
	{
		builder.append('L').append("dyvil/function/Function").append(this.parameterTypes.length).append(';');
	}

	@Override
	public void appendGenericSignature(StringBuilder builder)
	{
		builder.append('L').append("dyvil/function/Function").append(this.parameterTypes.length).append('<');
		int len = this.parameterTypes.length;
		if (len > 0)
		{
			this.parameterTypes[0].appendGenericSignature(builder);
			for (int i = 1; i < len; i++)
			{
				this.parameterTypes[i].appendGenericSignature(builder);
			}
		}
		this.returnType.appendGenericSignature(builder);
		builder.append('>').append(';');
	}
}
