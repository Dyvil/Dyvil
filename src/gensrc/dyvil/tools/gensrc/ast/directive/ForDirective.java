package dyvil.tools.gensrc.ast.directive;

import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.expression.Expression;
import dyvil.tools.gensrc.ast.scope.LazyScope;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.lang.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.PrintStream;

public class ForDirective implements Directive
{
	protected Name          varName;
	protected Expression    iterable;
	protected DirectiveList body;

	// Metadata
	protected SourcePosition position;

	public ForDirective(SourcePosition position)
	{
		this.position = position;
	}

	public Name getVarName()
	{
		return this.varName;
	}

	public void setVarName(Name varName)
	{
		this.varName = varName;
	}

	public Expression getIterable()
	{
		return this.iterable;
	}

	public void setIterable(Expression list)
	{
		this.iterable = list;
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
	public void specialize(GenSrc gensrc, Scope scope, MarkerList markers, PrintStream output)
	{
		for (Expression expr : this.iterable.evaluateIterable(scope))
		{
			final LazyScope innerScope = new LazyScope(scope);
			innerScope.define(this.varName.qualified, expr.evaluateString(scope));

			this.body.specialize(gensrc, innerScope, markers, output);
		}
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String indent, StringBuilder builder)
	{
		builder.append("#for (").append(this.varName).append(" <- ");
		if (this.iterable != null)
		{
			this.iterable.toString(indent, builder);
		}
		builder.append(')');

		if (this.body != null)
		{
			BasicDirective.appendBody(indent, builder, this.body);
		}
		else
		{
			builder.append('\n');
		}
	}
}
