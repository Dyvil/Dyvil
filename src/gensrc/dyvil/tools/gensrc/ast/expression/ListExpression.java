package dyvil.tools.gensrc.ast.expression;

import dyvil.source.position.SourcePosition;
import dyvil.tools.gensrc.ast.scope.Scope;

public class ListExpression implements Expression
{
	private SourcePosition position;
	private ExpressionList values = new ExpressionList();

	public ListExpression(SourcePosition position)
	{
		this.position = position;
	}

	public ExpressionList getValues()
	{
		return this.values;
	}

	public void setValues(ExpressionList values)
	{
		this.values = values;
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
	public String evaluateString(Scope scope)
	{
		return null;
	}

	@Override
	public Iterable<Expression> evaluateIterable(Scope scope)
	{
		return this.values;
	}

	@Override
	public void toString(String indent, StringBuilder builder)
	{
		builder.append("[ ");
		this.values.toString(indent, builder);
		builder.append(" ]");
	}
}
