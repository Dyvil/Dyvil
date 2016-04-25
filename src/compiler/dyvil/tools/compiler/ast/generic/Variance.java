package dyvil.tools.compiler.ast.generic;

import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public enum Variance
{
	INVARIANT
		{
			@Override
			public boolean checkCompatible(IType a, IType b)
			{
				return Types.isSameType(a, b);
			}
		},
	COVARIANT
		{
			@Override
			public boolean checkCompatible(IType a, IType b)
			{
				return Types.isSuperType(a, b);
			}

			@Override
			public void appendPrefix(StringBuilder builder)
			{
				builder.append('+');
			}

			@Override
			public void appendInfix(StringBuilder builder)
			{
				builder.append(" <: ");
			}
		},
	CONTRAVARIANT
		{
			@Override
			public boolean checkCompatible(IType a, IType b)
			{
				return Types.isSuperType(b, a);
			}

			@Override
			public void appendPrefix(StringBuilder builder)
			{
				builder.append('-');
			}

			@Override
			public void appendInfix(StringBuilder builder)
			{
				builder.append(" >: ");
			}
		};

	public abstract boolean checkCompatible(IType type1, IType type2);

	public static void write(Variance variance, DataOutput out) throws IOException
	{
		out.writeByte(variance.ordinal());
	}

	public static Variance read(DataInput in) throws IOException
	{
		switch (in.readByte())
		{
		case 0:
			return INVARIANT;
		case 1:
			return COVARIANT;
		case 2:
			return CONTRAVARIANT;
		}
		return INVARIANT;
	}

	public void appendPrefix(StringBuilder builder)
	{
	}

	public void appendInfix(StringBuilder builder)
	{
	}
}
