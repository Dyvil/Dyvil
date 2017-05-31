package dyvil.tools.gensrc.ast.directive;

import dyvil.source.position.SourcePosition;
import dyvil.tools.gensrc.ast.expression.ExpressionList;
import dyvil.tools.parsing.Name;

public abstract class BasicDirective implements Directive
{
	protected ExpressionList arguments = new ExpressionList();

	protected DirectiveList body;

	protected SourcePosition position;

	public BasicDirective()
	{
	}

	public BasicDirective(SourcePosition position)
	{
		this.position = position;
	}

	public abstract Name getName();

	public ExpressionList getArguments()
	{
		return this.arguments;
	}

	public void setArguments(ExpressionList arguments)
	{
		this.arguments = arguments;
	}

	public DirectiveList getBody()
	{
		return this.body;
	}

	public void setBody(DirectiveList body)
	{
		this.body = body;
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
	public void toString(String indent, StringBuilder builder)
	{
		builder.append('#').append(this.getName());
		if (this.arguments.size() > 0)
		{
			builder.append('(');
			this.arguments.toString(indent, builder);
			builder.append(')');

			if (this.body != null)
			{
				builder.append(' ');
			}
		}
		if (this.body != null)
		{
			builder.append('{');
			this.body.toString(indent + '\t', builder);
			builder.append('}').append('\n');
		}
	}
}
