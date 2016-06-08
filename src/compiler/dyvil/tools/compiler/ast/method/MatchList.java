package dyvil.tools.compiler.ast.method;

import dyvil.array.IntArray;
import dyvil.tools.compiler.ast.context.IImplicitContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.util.MemberSorter;

import java.util.Arrays;

public class MatchList<T extends ICallableSignature> implements IImplicitContext
{
	public static final class Candidate<T extends ICallableSignature> implements Comparable<Candidate<T>>
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
			boolean better = false;

			for (int i = 0, length = this.values.length; i < length; i++)
			{
				int conversion = compare(this.values[i], this.types[i], that.values[i], that.types[i]);

				if (conversion > 0)
				{
					// one conversion is worse
					return 1;
				}
				if (better || conversion < 0)
				{
					// one conversion is better
					better = true;
				}
			}
			if (better)
			{
				return -1;
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
			if (this == o)
			{
				return true;
			}
			if (!(o instanceof Candidate))
			{
				return false;
			}

			final Candidate<?> that = (Candidate<?>) o;
			return this.defaults == that.defaults && this.varargs == that.varargs //
				       && Arrays.equals(this.values, that.values) //
				       && this.member.isVariadic() == that.member.isVariadic();
		}

		@Override
		public int hashCode()
		{
			int result = Arrays.hashCode(this.values);
			result = 31 * result + this.defaults;
			result = 31 * result + this.varargs;
			result = 31 * result + (this.member.isVariadic() ? 1237 : 1231);
			return result;
		}
	}

	private Candidate<T>[] candidates = (Candidate<T>[]) new Candidate[4];
	private int size;

	private boolean sorted;

	private final IImplicitContext implicitContext;

	public MatchList(IImplicitContext implicitContext)
	{
		this.implicitContext = implicitContext;
	}

	public int size()
	{
		return this.size;
	}

	public boolean isEmpty()
	{
		return this.size <= 0;
	}

	public void ensureCapacity(int capacity)
	{
		if (capacity <= this.candidates.length)
		{
			return;
		}

		final int newCapacity = capacity << 1;

		final Candidate<T>[] tempMethods = (Candidate<T>[]) new Candidate[newCapacity];
		System.arraycopy(this.candidates, 0, tempMethods, 0, this.size);
		this.candidates = tempMethods;
	}

	public void add(Candidate<T> candidate)
	{
		final int index = this.size;
		this.ensureCapacity(index + 1);
		this.candidates[index] = candidate;
		this.size++;
	}

	public Candidate<T> getCandidate(int index)
	{
		return this.candidates[index];
	}

	public boolean isAmbigous()
	{
		if (this.size <= 1)
		{
			return false;
		}

		this.sort();
		return this.candidates[0].compareTo(this.candidates[1]) == 0;
	}

	public Candidate<T> getBestCandidate()
	{
		switch (this.size)
		{
		case 0:
			return null;
		case 1:
			return this.candidates[0];
		case 2:
			final Candidate<T> c1 = this.candidates[0];
			final Candidate<T> c2 = this.candidates[1];
			final int compareTo = c1.compareTo(c2);
			if (compareTo < 0)
			{
				return c1;
			}
			if (compareTo > 0)
			{
				return c2;
			}
			return null;
		}

		// this.sort(); // calling isAmbiguous() sorts this list
		if (this.isAmbigous())
		{
			return null;
		}
		return this.candidates[0];
	}

	public T getBestMember()
	{
		final Candidate<T> bestCandidate = this.getBestCandidate();
		return bestCandidate == null ? null : bestCandidate.member;
	}

	private void sort()
	{
		if (this.sorted || this.size <= 1)
		{
			return;
		}

		Arrays.sort(this.candidates, 0, this.size);
		this.sorted = true;
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		this.implicitContext.getImplicitMatches(list, value, targetType);
	}
}
