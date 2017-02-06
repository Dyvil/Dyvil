package dyvil.tools.gensrc.ast.directive;

import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.Specialization;
import dyvil.tools.gensrc.ast.Util;
import dyvil.tools.gensrc.ast.scope.LazyScope;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.PrintStream;

public class ImportDirective implements Directive
{
	private final ICodePosition position;
	private final String files;

	public ImportDirective(ICodePosition position, String files)
	{
		this.position = position;
		this.files = files;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, MarkerList markers, PrintStream output)
	{
		if (!(scope instanceof LazyScope))
		{
			return;
		}

		final LazyScope lazyScope = (LazyScope) scope;

		for (Specialization spec : Util.parseSpecs(this.files, gensrc, scope, markers, this.position))
		{
			lazyScope.importFrom(spec);
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
		builder.append(indent).append("#import ").append(this.files).append('\n');
	}
}
