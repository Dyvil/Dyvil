package dyvilx.tools.gensrc.ast.var;

import dyvil.io.AppendablePrintStream;
import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvilx.tools.parsing.marker.SemanticError;
import dyvilx.tools.gensrc.GenSrc;
import dyvilx.tools.gensrc.ast.directive.BasicDirective;
import dyvilx.tools.gensrc.ast.scope.LazyScope;
import dyvilx.tools.gensrc.ast.scope.Scope;
import dyvilx.tools.gensrc.lang.I18n;

import java.io.PrintStream;

public class DefineDirective extends VarDirective
{
	private boolean local;

	public DefineDirective(boolean local, SourcePosition position)
	{
		this.local = local;
		this.position = position;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, MarkerList markers, PrintStream output)
	{
		if (this.name == null)
		{
			markers.add(new SemanticError(this.position, I18n.get("define.name")));
			return;
		}

		final Scope defScope = this.local ? scope : scope.getGlobalParent();

		if (!(defScope instanceof LazyScope))
		{
			return;
		}

		final String value = this.computeValue(gensrc, scope, markers);
		((LazyScope) defScope).define(this.name.qualified, value);
	}

	private String computeValue(GenSrc gensrc, Scope scope, MarkerList markers)
	{
		if (this.body == null)
		{
			return "";
		}

		final StringBuilder builder = new StringBuilder();
		this.body.specialize(gensrc, new LazyScope(scope), markers, new AppendablePrintStream(builder));
		return builder.toString();
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String indent, StringBuilder builder)
	{
		builder.append(this.local ? "#local" : "#define").append('(').append(this.name).append(')');

		if (this.body != null)
		{
			BasicDirective.appendBody(indent, builder, this.body);
		}
		else
		{
			builder.append('\n');
		}
	}
}
