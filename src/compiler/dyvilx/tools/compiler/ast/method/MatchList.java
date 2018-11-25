package dyvilx.tools.compiler.ast.method;

import dyvil.collection.iterator.ArrayIterator;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.type.IType;

import java.util.*;

public class MatchList<T extends IOverloadable> implements IImplicitContext, Iterable<Candidate<T>>
{
	// =============== Constants ===============

	private static final int INITIAL_CAPACITY = 4;

	// =============== Fields ===============

	private Candidate<T>[] candidates = (Candidate<T>[]) new Candidate[INITIAL_CAPACITY];
	private int            size;

	private boolean sorted   = true;
	private boolean skipSort = false;

	private final IImplicitContext implicitContext;

	// =============== Constructors ===============

	public MatchList(IImplicitContext implicitContext)
	{
		this.implicitContext = implicitContext;
	}

	public MatchList(IImplicitContext implicitContext, boolean skipSort)
	{
		this.implicitContext = implicitContext;
		this.skipSort = skipSort;
	}

	// =============== Properties ===============

	public boolean isSkipSort()
	{
		return this.skipSort;
	}

	public int size()
	{
		return this.size;
	}

	public boolean isEmpty()
	{
		return this.size <= 0;
	}

	// =============== Methods ===============

	// --------------- Candidate Iteration ---------------

	@Override
	public Iterator<Candidate<T>> iterator()
	{
		this.sort();
		return new ArrayIterator<>(this.candidates, 0, this.size);
	}

	// --------------- Accessing Candidates ---------------

	public boolean hasCandidate()
	{
		return !this.isSkipSort() && !this.isEmpty() && !this.isAmbigous() && !this.getBestCandidate().invalid;
	}

	public Candidate<T> getCandidate(int index)
	{
		this.sort();
		return this.candidates[index];
	}

	public Candidate<T> getBestCandidate()
	{
		switch (this.size)
		{
		case 0:
			return null;
		case 1:
			return this.candidates[0];
		default:
			// this.sort(); // calling isAmbiguous() sorts this list
			return this.isAmbigous() ? null : this.candidates[0];
		}
	}

	public T getBestMember()
	{
		final Candidate<T> bestCandidate = this.getBestCandidate();
		return bestCandidate == null ? null : bestCandidate.member;
	}

	// --------------- Ambiguity ---------------

	public boolean isAmbigous()
	{
		// faster version of:
		// return this.getAmbiguousCandidates().size() != 1

		if (this.size <= 1 || this.skipSort)
		{
			return false;
		}

		this.sort();

		final Candidate<T> first = this.candidates[0];
		for (int i = 1; i < this.size; i++)
		{
			final Candidate<T> candidate = this.candidates[i];
			if (candidate.member != first.member && first.equals(candidate))
			{
				// if two candidates have the same rank but different members, its ambiguous
				// sometimes the same member is added twice, but we don't count that as ambiguous.
				return true;
			}
		}

		return false;
	}

	public List<Candidate<T>> getAmbiguousCandidates()
	{
		if (this.size <= 1 || this.skipSort)
		{
			return Collections.emptyList();
		}

		this.sort();

		final List<Candidate<T>> result = new ArrayList<>();
		final Candidate<T> first = this.candidates[0];
		result.add(first);

		for (int i = 1; i < this.size; i++)
		{
			final Candidate<T> candidate = this.candidates[i];
			if (candidate.member != first.member && first.equals(candidate))
			{
				// if two candidates have the same rank but different members, its ambiguous
				// sometimes the same member is added twice, but we don't count that as ambiguous.
				result.add(candidate);
			}
		}
		return Collections.unmodifiableList(result);
	}

	// --------------- Adding Candidates ---------------

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
		this.sorted = false;

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

		this.sorted = false;
		this.ensureCapacity(this.size + otherSize);
		System.arraycopy(list.candidates, 0, this.candidates, this.size, otherSize);
		this.size += otherSize;
	}

	// --------------- Sorting Candidates ---------------

	private void sort()
	{
		if (this.sorted || this.skipSort || this.size <= 1)
		{
			return;
		}

		Arrays.sort(this.candidates, 0, this.size);
		this.sorted = true;
	}

	// --------------- Implicit Resolution ---------------

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		this.implicitContext.getImplicitMatches(list, value, targetType);
	}

	// --------------- Copying ---------------

	public MatchList<T> emptyCopy()
	{
		return new MatchList<>(this.implicitContext, this.skipSort);
	}
}
