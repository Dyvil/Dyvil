package dyvil.tools.compiler.ast.method;

import dyvil.collection.iterator.ArrayIterator;
import dyvil.tools.compiler.ast.context.IImplicitContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.type.IType;

import java.util.Arrays;
import java.util.Iterator;

public class MatchList<T extends ICallableSignature> implements IImplicitContext, Iterable<Candidate<T>>
{
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

	public boolean hasCandidate()
	{
		return !this.isEmpty() && !this.isAmbigous() && !this.getBestCandidate().invalid;
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

	@Override
	public Iterator<Candidate<T>> iterator()
	{
		this.sort();
		return new ArrayIterator<>(this.candidates, 0, this.size);
	}
}
