package dyvil.tools.gensrc.ast.var;

import dyvil.source.position.SourcePosition;
import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.scope.LazyScope;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.gensrc.lang.I18n;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.marker.SemanticError;

import java.io.PrintStream;

public class UndefineDirective extends VarDirective
{
	private final boolean local;

	public UndefineDirective(boolean local, SourcePosition position)
	{
		this.local = local;
		this.position = position;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, MarkerList markers, PrintStream output)
	{
		if (this.name == null)
		{
			markers.add(new SemanticError(this.position, I18n.get("undefine.name")));
			return;
		}

		final Scope defScope = this.local ? scope : scope.getGlobalParent();

		if (!(defScope instanceof LazyScope))
		{
			return;
		}

		((LazyScope) defScope).undefine(this.name.qualified);
	}

	@Override
	public void toString(String indent, StringBuilder builder)
	{
		builder.append(this.local ? "#delete(" : "#undefine(").append(this.name).append(")\n");
	}
}
