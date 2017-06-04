package dyvil.tools.gensrc.ast.expression;

import dyvil.source.position.SourcePosition;
import dyvil.tools.gensrc.ast.scope.Scope;

public class DoubleValue implements Expression
{
	private SourcePosition position;
	private final double value;

	public DoubleValue(SourcePosition position, double value)
	{
		this.position = position;
		this.value = value;
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

	@Override
	public boolean evaluateBoolean(Scope scope)
	{
		return this.value != 0;
	}

	@Override
	public long evaluateInteger(Scope scope)
	{
		return (long) this.value;
	}

	@Override
	public double evaluateDouble(Scope scope)
	{
		return this.value;
	}

	@Override
	public String evaluateString(Scope scope)
	{
		return Double.toString(this.value);
	}

	@Override
	public void toString(String indent, StringBuilder builder)
	{
		builder.append(this.value);
	}
}
