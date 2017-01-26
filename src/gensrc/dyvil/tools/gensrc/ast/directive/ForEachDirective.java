package dyvil.tools.gensrc.ast.directive;

import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.scope.LazyScope;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.gensrc.ast.Specialization;
import dyvil.tools.gensrc.ast.Util;

import java.io.PrintStream;

public class ForEachDirective implements Directive
{
	private final String list;

	private Directive action;

	public ForEachDirective(String list)
	{
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
	public void specialize(GenSrc gensrc, Scope scope, PrintStream output)
	{
		final Specialization[] specs = Util.parseSpecs(this.list, gensrc, scope);

		for (Specialization spec : specs)
		{
			final LazyScope innerScope = new LazyScope(scope);
			innerScope.importFrom(spec);

			this.action.specialize(gensrc, innerScope, output);
		}
	}
}
