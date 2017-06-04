package dyvil.tools.gensrc.ast.var;

import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.directive.BasicDirective;
import dyvil.tools.gensrc.ast.scope.LazyScope;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.parsing.marker.MarkerList;

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
