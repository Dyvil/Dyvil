package dyvil.tools.gensrc.ast.directive;

import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.Specialization;
import dyvil.tools.gensrc.ast.Util;
import dyvil.tools.gensrc.ast.scope.LazyScope;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.PrintStream;

public class ForEachDirective implements Directive
{
	private final ICodePosition position;
	private final String list;

	private Directive action;

	public ForEachDirective(ICodePosition position, String list)
	{
		this.position = position;
		this.list = list;
	}

	public Directive getAction()
	{
		return this.action;
	}

	public void setAction(Directive action)
	{
		this.action = action;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, MarkerList markers, PrintStream output)
	{
		for (Specialization spec : Util.parseSpecs(this.list, gensrc, scope, markers, this.position))
		{
			final LazyScope innerScope = new LazyScope(scope);
			innerScope.importFrom(spec);

			this.action.specialize(gensrc, innerScope, markers, output);
		}
	}

	@Override
	public String toString()
	{
		return Directive.toString(this);
	}

	@Override
	public void toString(String indent, StringBuilder builder)
	{
		builder.append(indent).append("#foreach ").append(this.list).append('\n');

		this.action.toString(indent + '\t', builder);

		builder.append(indent).append("#end\n");
	}
}
