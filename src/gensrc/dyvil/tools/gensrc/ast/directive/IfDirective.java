package dyvil.tools.gensrc.ast.directive;

import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.expression.Expression;
import dyvil.tools.gensrc.ast.scope.LazyScope;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.PrintStream;

public class IfDirective implements Directive
{
	private SourcePosition position;

	private Expression condition;
	private Directive  thenBlock;
	private Directive  elseBlock;

	public IfDirective(SourcePosition position)
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

	public Expression getCondition()
	{
		return this.condition;
	}

	public void setCondition(Expression condition)
	{
		this.condition = condition;
	}

	public Directive getThenBlock()
	{
		return this.thenBlock;
	}

	public void setThenBlock(Directive thenBlock)
	{
		this.thenBlock = thenBlock;
	}

	public Directive getElseBlock()
	{
		return this.elseBlock;
	}

	public void setElseBlock(Directive elseBlock)
	{
		this.elseBlock = elseBlock;
	}

	private boolean evaluate(Scope scope)
	{
		return this.condition.evaluateBoolean(scope);
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, MarkerList markers, PrintStream output)
	{
		if (this.evaluate(scope))
		{
			this.thenBlock.specialize(gensrc, new LazyScope(scope), markers, output);
		}
		else if (this.elseBlock != null)
		{
			this.elseBlock.specialize(gensrc, new LazyScope(scope), markers, output);
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
		// #if(condition) {then-block} #else {else-block}

		builder.append("#if (");
		this.condition.toString(indent, builder);
		builder.append(")");

		if (this.thenBlock != null)
		{
			BasicDirective.appendBody(indent, builder, this.thenBlock);
		}

		if (this.elseBlock != null)
		{
			builder.append(indent).append("#else");
			BasicDirective.appendBody(indent, builder, this.elseBlock);
		}
	}
}
