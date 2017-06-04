package dyvil.tools.gensrc.ast.expression;

import dyvil.source.position.SourcePosition;
import dyvil.tools.gensrc.ast.scope.Scope;

import java.util.Iterator;

public class RangeOperator implements Expression
{
	private SourcePosition position;

	private Expression start;
	private Expression end;

	public RangeOperator(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	public Expression getStart()
	{
		return this.start;
	}

	public void setStart(Expression start)
	{
		this.start = start;
	}

	public Expression getEnd()
	{
		return this.end;
	}

	public void setEnd(Expression end)
	{
		this.end = end;
	}

	@Override
	public String evaluateString(Scope scope)
	{
		return this.start.evaluateString(scope) + " .. " + this.end.evaluateString(scope);
	}

	@Override
	public Iterable<Expression> evaluateIterable(Scope scope)
	{
		final long start = this.start.evaluateInteger(scope);
		final long end = this.end.evaluateInteger(scope);

		return () -> new Iterator<Expression>()
		{
			long current = start;

			@Override
			public boolean hasNext()
			{
				return this.current <= end;
			}

			@Override
			public Expression next()
			{
				return new LongValue(null, this.current++);
			}
		};
	}

	@Override
	public void toString(String indent, StringBuilder builder)
	{
		this.start.toString(indent, builder);
		builder.append(" .. ");
		this.end.toString(indent, builder);
	}
}
