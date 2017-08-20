package dyvilx.tools.gensrc.ast.expression;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.gensrc.ast.scope.Scope;

public class LongValue implements Expression
{
	private SourcePosition position;
	private final long value;

	public LongValue(SourcePosition position, long value)
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
		return this.value;
	}

	@Override
	public double evaluateDouble(Scope scope)
	{
		return this.value;
	}

	@Override
	public String evaluateString(Scope scope)
	{
		return Long.toString(this.value);
	}

	@Override
	public void toString(String indent, StringBuilder builder)
	{
		builder.append(this.value);
	}
}
