package dyvil.tools.gensrc.ast.expression;

import dyvil.collection.iterator.ArrayIterator;
import dyvil.tools.gensrc.ast.scope.Scope;

import java.util.Iterator;

public class ExpressionList implements Iterable<Expression>
{
	private int size;

	private Expression[] array;

	public ExpressionList()
	{
		this(10);
	}

	public ExpressionList(int capacity)
	{
		this.array = new Expression[capacity];
	}

	public ExpressionList(Expression... elements)
	{
		this.array = elements;
		this.size = elements.length;
	}

	public int size()
	{
		return this.size;
	}

	@Override
	public Iterator<Expression> iterator()
	{
		return new ArrayIterator<>(this.array, this.size);
	}

	public Expression get(int index)
	{
		return this.array[index];
	}

	protected void ensureCapacity(int capacity)
	{
		if (capacity >= this.array.length)
		{
			final Expression[] temp = new Expression[capacity];
			System.arraycopy(this.array, 0, temp, 0, this.size);
			this.array = temp;
		}
	}

	public void add(Expression expr)
	{
		this.ensureCapacity(this.size + 1);
		this.array[this.size++] = expr;
	}

	public void addAll(Iterable<Expression> iterable)
	{
		if (iterable instanceof ExpressionList)
		{
			this.addAll((ExpressionList) iterable);
			return;
		}

		for (Expression expr : iterable)
		{
			this.add(expr);
		}
	}

	public void addAll(ExpressionList list)
	{
		this.ensureCapacity(this.size + list.size);
		System.arraycopy(list.array, 0, this.array, this.size, list.size);
		this.size += list.size;
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		this.toString("", builder);
		return builder.toString();
	}

	public void toString(String indent, StringBuilder builder)
	{
		if (this.size == 0)
		{
			return;
		}

		this.array[0].toString(indent, builder);
		for (int i = 1; i < this.size; i++)
		{
			builder.append(", ");
			this.array[i].toString(indent, builder);
		}
	}

	public ExpressionList flatten(Scope scope)
	{
		final ExpressionList result = new ExpressionList(this.size);
		for (int i = 0; i < this.size; i++)
		{
			result.addAll(this.array[i].evaluateIterable(scope));
		}
		return result;
	}
}
