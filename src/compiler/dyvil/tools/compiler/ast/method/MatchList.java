package dyvil.tools.compiler.ast.method;

import dyvil.tools.compiler.ast.context.IImplicitContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.type.IType;

public class MatchList<T> implements IImplicitContext
{
	private T[]     methods = (T[]) new Object[4];
	private float[] values  = new float[4];
	private int size;

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
		if (capacity <= this.methods.length)
		{
			return;
		}

		final int newCapacity = capacity << 1;

		final T[] tempMethods = (T[]) new Object[newCapacity];
		System.arraycopy(this.methods, 0, tempMethods, 0, this.size);
		this.methods = tempMethods;

		final float[] tempValues = new float[newCapacity];
		System.arraycopy(this.values, 0, tempValues, 0, this.size);
		this.values = tempValues;
	}

	public void add(T method, float match)
	{
		this.ensureCapacity(this.size + 1);
		this.methods[this.size] = method;
		this.values[this.size] = match;
		this.size++;
	}

	public float getValue(int index)
	{
		return this.values[index];
	}

	public T getCandidate(int index)
	{
		return this.methods[index];
	}

	public int getBestIndex()
	{
		switch (this.size)
		{
		case 0:
			return -1;
		case 1:
			return 0;
		}

		int bestIndex = 0;
		float bestMatch = this.values[0];
		for (int i = 1; i < this.size; i++)
		{
			float match = this.values[i];
			if (match < bestMatch)
			{
				bestIndex = i;
				bestMatch = match;
			}
		}

		return bestIndex;
	}

	public float getBestValue()
	{
		final int bestIndex = this.getBestIndex();
		if (bestIndex < 0)
		{
			return 0F;
		}
		return this.values[bestIndex];
	}

	public T getBestCandidate()
	{
		final int bestIndex = this.getBestIndex();
		if (bestIndex < 0)
		{
			return null;
		}

		return this.methods[bestIndex];
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		this.implicitContext.getImplicitMatches(list, value, targetType);
	}
}
