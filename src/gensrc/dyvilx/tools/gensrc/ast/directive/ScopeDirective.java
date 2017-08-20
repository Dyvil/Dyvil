package dyvilx.tools.gensrc.ast.directive;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.gensrc.GenSrc;
import dyvilx.tools.gensrc.ast.scope.LazyScope;
import dyvilx.tools.gensrc.ast.scope.Scope;
import dyvilx.tools.gensrc.lang.I18n;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvilx.tools.parsing.marker.SemanticError;

import java.io.PrintStream;

public class ScopeDirective extends BasicDirective
{
	private static final Name NAME = Name.fromRaw("");

	public ScopeDirective(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public Name getName()
	{
		return NAME;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, MarkerList markers, PrintStream output)
	{
		int argCount = this.arguments.size();
		switch (argCount)
		{
		case 0:
			break;
		case 1:
			output.print(this.arguments.get(0).evaluateString(scope));
			break;
		default:
			markers.add(new SemanticError(this.position, I18n.get("scope.arguments")));
		}
		if (this.body != null)
		{
			this.body.specialize(gensrc, new LazyScope(scope), markers, output);
		}
	}
}
