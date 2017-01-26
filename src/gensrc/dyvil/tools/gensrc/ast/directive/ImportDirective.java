package dyvil.tools.gensrc.ast.directive;

import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.scope.LazyScope;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.gensrc.ast.Specialization;
import dyvil.tools.gensrc.ast.Util;

import java.io.PrintStream;

public class ImportDirective implements Directive
{
	private final String files;

	public ImportDirective(String files)
	{
		this.files = files;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, PrintStream output)
	{
		if (!(scope instanceof LazyScope))
		{
			return;
		}

		final LazyScope lazyScope = (LazyScope) scope;

		final Specialization[] specs = Util.parseSpecs(this.files, gensrc, scope);
		for (Specialization spec : specs)
		{
			lazyScope.importFrom(spec);
		}
	}
}
