package dyvilx.tools.gensrc.ast.directive;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.gensrc.GenSrc;
import dyvilx.tools.gensrc.ast.Specialization;
import dyvilx.tools.gensrc.ast.expression.Expression;
import dyvilx.tools.gensrc.ast.scope.LazyScope;
import dyvilx.tools.gensrc.ast.scope.Scope;
import dyvilx.tools.gensrc.lang.I18n;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvilx.tools.parsing.marker.SemanticError;

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

			if (spec == null)
			{
				markers.add(new SemanticError(expr.getPosition(), I18n.get("import.spec.resolve", reference)));
				continue;
			}

			lazyScope.importFrom(spec);
		}
	}
}
