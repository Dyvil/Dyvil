package dyvil.tools.gensrc.ast.directive;

import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.expression.Expression;
import dyvil.tools.gensrc.ast.scope.LazyScope;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.PrintStream;

public class ForDirective implements Directive
{
	protected Name          varName;
	protected Expression    list;
	protected DirectiveList block;

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

	public Expression getList()
	{
		return this.list;
	}

	public void setList(Expression list)
	{
		this.list = list;
	}

	public DirectiveList getBlock()
	{
		return this.block;
	}

	public void setBlock(DirectiveList block)
	{
		this.block = block;
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
		for (Expression expr : this.list.evaluateList(scope))
		{
			final LazyScope innerScope = new LazyScope(scope);
			innerScope.define(this.varName.qualified, expr.evaluateString(scope));

			this.block.specialize(gensrc, innerScope, markers, output);
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
		builder.append("#for(").append(this.varName).append(" <- ").append(this.list).append(") {");
		this.block.toString(indent + '\t', builder);
		builder.append('}');
	}
}
