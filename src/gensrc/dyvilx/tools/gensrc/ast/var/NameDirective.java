package dyvilx.tools.gensrc.ast.var;

import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvilx.tools.gensrc.GenSrc;
import dyvilx.tools.gensrc.ast.directive.BasicDirective;
import dyvilx.tools.gensrc.ast.scope.LazyScope;
import dyvilx.tools.gensrc.ast.scope.Scope;

import java.io.PrintStream;

public class NameDirective extends VarDirective
{
	public NameDirective(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, MarkerList markers, PrintStream output)
	{
		output.print(this.name.qualified);

		if (this.body != null)
		{
			final LazyScope newScope = new LazyScope(scope);
			newScope.undefine(this.name.qualified);

			this.body.specialize(gensrc, newScope, markers, output);
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
		builder.append("#name(").append(this.name).append(')');

		if (this.body != null)
		{
			BasicDirective.appendBody(indent, builder, this.body);
		}
	}
}
