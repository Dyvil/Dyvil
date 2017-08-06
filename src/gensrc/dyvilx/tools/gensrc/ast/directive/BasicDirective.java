package dyvilx.tools.gensrc.ast.directive;

import dyvil.annotation.internal.NonNull;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.gensrc.ast.expression.ExpressionList;
import dyvil.lang.Name;

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

	public boolean isStatement()
	{
		return this.body != null;
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
		}

		if (this.body != null)
		{
			appendBody(indent, builder, this.body);
		}
		else if (this.isStatement())
		{
			builder.append('\n');
		}
	}

	public static void appendBody(@NonNull String indent, @NonNull StringBuilder builder, @NonNull Directive body)
	{
		builder.append(" {\n");
		body.toString(indent + '\t', builder);
		builder.append("}\n");
	}
}
