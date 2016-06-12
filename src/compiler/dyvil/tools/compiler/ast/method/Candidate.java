package dyvil.tools.compiler.ast.method;

import dyvil.array.IntArray;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.util.MemberSorter;

public final class Candidate<T extends ICallableSignature> implements Comparable<Candidate<T>>
{
	protected final T       member;
	protected final int[]   values;
	protected final IType[] types;
	protected final int     defaults;
	protected final int     varargs;

	public Candidate(T member)
	{
		this.member = member;
		this.defaults = this.varargs = 0;
		this.values = IntArray.EMPTY;
		this.types = new IType[0];
	}

	public Candidate(T member, int value1, IType type1)
	{
		this.member = member;
		this.values = new int[] { value1 };
		this.types = new IType[] { type1 };
		this.defaults = this.varargs = 0;
	}

	public Candidate(T member, int[] values, IType[] types)
	{
		this.member = member;
		this.values = values;
		this.types = types;
		this.defaults = this.varargs = 0;
	}

	public Candidate(T member, int defaults, int varargs, int[] values, IType[] types)
	{
		this.member = member;
		this.values = values;
		this.types = types;
		this.defaults = defaults;
		this.varargs = varargs;
	}

	public T getMember()
	{
		return this.member;
	}

	public double getValue(int arg)
	{
		return this.values[arg];
	}

	@Override
	public int compareTo(Candidate<T> that)
	{
		int better = 0;
		int worse = 0;

		for (int i = 0, length = this.values.length; i < length; i++)
		{
			int conversion = compare(this.values[i], this.types[i], that.values[i], that.types[i]);

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
			return -1;
		}
		if (better < worse)
		{
			return 1;
		}

		// Compare number of defaulted parameters (less is better)
		final int defaults = Integer.compare(this.defaults, that.defaults);
		if (defaults != 0)
		{
			return defaults;
		}

		// Compare number of varargs-applied arguments (less is better)
		final int varargs = Integer.compare(this.varargs, that.varargs);
		if (varargs != 0)
		{
			return varargs;
		}

		// Compare varargs (the non-variadic method is preferred)
		return Boolean.compare(this.member.isVariadic(), that.member.isVariadic());
	}

	private static int compare(int value1, IType type1, int value2, IType type2)
	{
		if (value1 < value2)
		{
			return 1;
		}
		if (value1 > value2)
		{
			return -1;
		}
		return MemberSorter.compareTypes(type1, type2);
	}

	@Override
	public boolean equals(Object o)
	{
		return this == o || o instanceof Candidate && this.compareTo((Candidate<T>) o) == 0;
	}

	@Override
	public int hashCode()
	{
		// we cannot provide a meaningful hashCode without breaking the equality contract
		return 0;
	}
}
