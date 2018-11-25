package dyvilx.tools.compiler.ast.method;

import dyvil.annotation.internal.NonNull;
import dyvil.array.IntArray;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.util.MemberSorter;

import java.util.Arrays;

public final class Candidate<T extends IOverloadable> implements Comparable<Candidate<T>>
{
	protected final T       member;
	protected final int[]   values;
	protected final IType[] types;
	protected final int     defaults;
	protected final int     varargs;
	protected final boolean invalid;

	public Candidate(T member)
	{
		this(member, false);
	}

	public Candidate(T member, boolean invalid)
	{
		this(member, IntArray.EMPTY, new IType[0], 0, 0, invalid);
	}

	public Candidate(T member, int value1, IType type1, boolean invalid)
	{
		this(member, new int[] { value1 }, new IType[] { type1 }, 0, 0, invalid);
	}

	public Candidate(T member, int[] values, IType[] types)
	{
		this(member, values, types, 0, 0, false);
	}

	public Candidate(T member, int[] values, IType[] types, int defaults, int varargs)
	{
		this(member, values, types, defaults, varargs, false);
	}

	public Candidate(T member, int[] values, IType[] types, int defaults, int varargs, boolean invalid)
	{
		this.member = member;
		this.values = values;
		this.types = types;
		this.defaults = defaults;
		this.varargs = varargs;
		this.invalid = invalid;
	}

	public T getMember()
	{
		return this.member;
	}

	public double getValue(int arg)
	{
		return this.values[arg];
	}

	/**
	 * A candidate compares less than another if it is better, and greater if it is worse.
	 *
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(@NonNull Candidate<T> o)
	{
		// Compare invalidity (valid is better)
		final int byInvalid = Boolean.compare(this.invalid, o.invalid);
		if (byInvalid != 0)
		{
			return byInvalid;
		}

		int better = 0;
		int worse = 0;

		for (int i = 0, length = this.values.length; i < length; i++)
		{
			final int conversion = compare(this.values[i], this.types[i], o.values[i], o.types[i]);
			if (conversion > 0)
			{
				worse++;
			}
			if (conversion < 0)
			{
				better++;
			}
		}
		if (better > worse)
		{
			// this has more better than worse argument conversions, so this is better
			return -1;
		}
		if (better < worse)
		{
			// this has more worse and better argument conversions, so this is worse
			return 1;
		}

		// Compare return types (more specific is better)
		final int byReturnType = MemberSorter.compareTypes(this.member.getType(), o.member.getType());
		if (byReturnType != 0)
		{
			return byReturnType;
		}

		// Compare number of defaulted parameters (less is better)
		final int byDefaults = Integer.compare(this.defaults, o.defaults);
		if (byDefaults != 0)
		{
			return byDefaults;
		}

		// Compare number of varargs-applied arguments (less is better)
		final int byVarargs = Integer.compare(this.varargs, o.varargs);
		if (byVarargs != 0)
		{
			return byVarargs;
		}

		// Compare varargs (not variadic is better)
		final int byVariadic = Boolean.compare(this.member.isVariadic(), o.member.isVariadic());
		if (byVariadic != 0)
		{
			return byVariadic;
		}

		// Compare overload priority (greater is better)
		return -Integer.compare(this.member.getOverloadPriority(), o.member.getOverloadPriority());
	}

	private static int compare(int thisMatch, IType type1, int thatMatch, IType type2)
	{
		// for thisMatch and thatMatch, greater is better
		if (thisMatch < thatMatch)
		{
			// thisMatch is less, so this is worse
			return 1;
		}
		if (thisMatch > thatMatch)
		{
			// thisMatch is greater, so this is better
			return -1;
		}

		// compare parameter type (more specific is better)
		return MemberSorter.compareTypes(type1, type2);
	}

	@Override
	public boolean equals(Object obj)
	{
		return this == obj || obj instanceof Candidate && this.compareTo((Candidate<T>) obj) == 0;
	}

	@Override
	public int hashCode()
	{
		// we cannot provide a meaningful hashCode without breaking the equality contract
		return 0;
	}

	@Override
	public String toString()
	{
		return "Candidate(" + "member: " + this.member + ", values: " + Arrays.toString(this.values) + ", types: "
			       + Arrays.toString(this.types) + ", defaults: " + this.defaults + ", varargs: " + this.varargs
			       + ", invalid: " + this.invalid + ')';
	}
}
