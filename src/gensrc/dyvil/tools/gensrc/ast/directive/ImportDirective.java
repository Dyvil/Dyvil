package dyvil.tools.gensrc.ast.directive;

import dyvil.source.position.SourcePosition;
import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.Specialization;
import dyvil.tools.gensrc.ast.expression.Expression;
import dyvil.tools.gensrc.ast.scope.LazyScope;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.PrintStream;

public class ImportDirective extends BasicDirective
{
	public static final Name IMPORT = Name.fromQualified("import");

	public ImportDirective(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public Name getName()
	{
		return IMPORT;
	}

	@Override
	public boolean isStatement()
	{
		return true;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, MarkerList markers, PrintStream output)
	{
		if (!(scope instanceof LazyScope))
		{
			return;
		}

		final LazyScope lazyScope = (LazyScope) scope;

		for (Expression expr : this.arguments)
		{
			final String reference = expr.evaluateString(scope);
			final Specialization spec = Specialization.resolveSpec(reference, scope.getSourceFile(), gensrc);
			lazyScope.importFrom(spec);
		}
	}
}
