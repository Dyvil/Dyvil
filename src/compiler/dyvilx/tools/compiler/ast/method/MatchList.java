package dyvilx.tools.compiler.ast.method;

import dyvil.collection.iterator.ArrayIterator;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.type.IType;

import java.util.Arrays;
import java.util.Iterator;

public class MatchList<T extends IOverloadable> implements IImplicitContext, Iterable<Candidate<T>>
{
	private static final byte SORTED    = 1;
	private static final byte SKIP_SORT = 2;

	private Candidate<T>[] candidates = (Candidate<T>[]) new Candidate[4];
	private int size;

	private byte sorted = SORTED;

	private final IImplicitContext implicitContext;

	public MatchList(IImplicitContext implicitContext)
	{
		this.implicitContext = implicitContext;
	}

	public MatchList(IImplicitContext implicitContext, boolean skipSort)
	{
		this.implicitContext = implicitContext;

		if (skipSort)
		{
			this.sorted |= SKIP_SORT;
		}
	}

	public boolean isSkipSort()
	{
		return (this.sorted & SKIP_SORT) != 0;
	}

	public int size()
	{
		return this.size;
	}

	public boolean isEmpty()
	{
		return this.size <= 0;
	}

	public boolean hasCandidate()
	{
		return !this.isSkipSort() && !this.isEmpty() && !this.isAmbigous() && !this.getBestCandidate().invalid;
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
		this.sorted &= ~SORTED;

		this.ensureCapacity(this.size + 1);
		this.candidates[this.size++] = candidate;
	}

	public void addAll(MatchList<T> list)
	{
		final int otherSize = list.size;
		if (otherSize <= 0)
		{
			return;
		}

		this.sorted &= ~SORTED;
		this.ensureCapacity(this.size + otherSize);
		System.arraycopy(list.candidates, 0, this.candidates, this.size, otherSize);
		this.size += otherSize;
	}

	public Candidate<T> getCandidate(int index)
	{
		return this.candidates[index];
	}

	public boolean isAmbigous()
	{
		if (this.size <= 1 || (this.sorted & SKIP_SORT) != 0)
		{
			return false;
		}

		this.sort();

		final Candidate<T> first = this.candidates[0];
		for (int i = 1; i < this.size; i++)
		{
			final Candidate<T> candidate = this.candidates[i];
			if (candidate.member != first.member && first.compareTo(candidate) == 0)
			{
				// if two candidates have the same rank but different members, its ambiguous
				// sometimes the same member is added twice, but we don't count that as ambiguous.
				return true;
			}
		}

		return false;
	}

	public Candidate<T> getBestCandidate()
	{
		switch (this.size)
		{
		case 0:
			return null;
		case 1:
			return this.candidates[0];
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
		if (this.sorted != 0 || this.size <= 1)
		{
			return;
		}

		Arrays.sort(this.candidates, 0, this.size);
		this.sorted |= SORTED;
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		this.implicitContext.getImplicitMatches(list, value, targetType);
	}

	@Override
	public Iterator<Candidate<T>> iterator()
	{
		this.sort();
		return new ArrayIterator<>(this.candidates, 0, this.size);
	}

	public MatchList<T> emptyCopy()
	{
		return new MatchList<>(this.implicitContext, this.isSkipSort());
	}
}
