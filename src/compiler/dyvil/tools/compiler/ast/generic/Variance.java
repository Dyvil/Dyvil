package dyvil.tools.compiler.ast.generic;

import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.WildcardType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@SuppressWarnings("StandardVariableNames")
public enum Variance
{
	INVARIANT
		{
			@Override
			public boolean checkCompatible(IType a, IType b)
			{
				return Types.isSameType(a, b);
			}
		}, //
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
		}, //
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
		};

	public abstract boolean checkCompatible(IType a, IType b);

	public static boolean checkCompatible(Variance base, IType a, IType b)
	{
		final WildcardType wildcard1 = a.extract(WildcardType.class);

		if (wildcard1 != null)
		{
			final WildcardType wildcard2 = b.extract(WildcardType.class);
			if (wildcard2 != null)
			{
				return wildcard1.getVariance().checkCompatible(wildcard1.getType(), wildcard2.getType());
			}

			return wildcard1.getVariance().checkCompatible(wildcard1.getType(), b);
		}

		final WildcardType wildcard2 = b.extract(WildcardType.class);
		if (wildcard2 != null)
		{
			return wildcard2.getVariance().checkCompatible(wildcard2.getType(), a);
		}

		return base.checkCompatible(a, b);
	}

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
}
